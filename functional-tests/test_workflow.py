"""Run from repository root. Uses a separate transient app and test database."""
import os
import socket
import subprocess
import tempfile
import time
import unittest
import urllib.request
from datetime import datetime, timedelta, timezone
from pathlib import Path
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import Select, WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

ROOT = Path(__file__).resolve().parents[1]

class WorkflowTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        jars = list((ROOT / 'target').glob('task-tracker-*.jar'))
        if not jars:
            raise RuntimeError('Build first with the Maven wrapper package command.')
        with socket.socket() as sock:
            sock.bind(('127.0.0.1', 0))
            port = sock.getsockname()[1]
        cls.url = f'http://127.0.0.1:{port}'
        cls.log = tempfile.TemporaryFile()
        cls.app = subprocess.Popen([
            os.environ.get('JAVA_EXE', 'java'), '-jar', str(jars[0]),
            f'--server.port={port}', '--spring.datasource.url=jdbc:h2:mem:selenium',
            '--spring.jpa.hibernate.ddl-auto=create-drop'
        ], cwd=ROOT, stdout=cls.log, stderr=subprocess.STDOUT)
        cls.addClassCleanup(cls.stop_app)
        deadline = time.monotonic() + 90
        while time.monotonic() < deadline:
            if cls.app.poll() is not None:
                raise RuntimeError('Test application exited. Check the local Java installation.')
            try:
                if urllib.request.urlopen(cls.url, timeout=2).status == 200:
                    break
            except Exception:
                time.sleep(.3)
        else:
            raise RuntimeError('Application did not start within 90 seconds.')
        options = webdriver.ChromeOptions()
        options.add_argument('--headless=new')
        options.add_argument('--window-size=1440,1000')
        options.add_argument('--remote-debugging-pipe')
        if os.environ.get('TEST_BROWSER_PROFILE'):
            options.add_argument('--user-data-dir=' + os.environ['TEST_BROWSER_PROFILE'])
        if os.environ.get('CHROME_BINARY'):
            options.binary_location = os.environ['CHROME_BINARY']
        service = Service(os.environ['CHROMEDRIVER']) if os.environ.get('CHROMEDRIVER') else Service()
        cls.driver = webdriver.Chrome(service=service, options=options)
        cls.addClassCleanup(cls.driver.quit)
        cls.wait = WebDriverWait(cls.driver, 10)

    @classmethod
    def stop_app(cls):
        cls.app.terminate()
        try:
            cls.app.wait(timeout=10)
        except subprocess.TimeoutExpired:
            cls.app.kill()
        cls.log.close()

    def element(self, selector):
        return self.driver.find_element(By.CSS_SELECTOR, selector)

    def click_and_wait(self, selector):
        old = self.element('body')
        self.element(selector).click()
        self.wait.until(EC.staleness_of(old))

    def test_add_edit_cancel_complete_filter_and_validation(self):
        self.driver.get(self.url)
        self.assertIn('No tasks yet', self.element('body').text)
        self.element('#title').send_keys('Coursework')
        self.element('#description').send_keys('Prepare backlog')
        Select(self.element('#category')).select_by_visible_text('College')
        self.click_and_wait('#save-task')
        self.assertEqual('Coursework', self.element('.task-title').text)
        self.assertEqual('1', self.element('#pending-count').text)

        self.click_and_wait('.edit-task')
        self.element('#title').clear()
        self.element('#title').send_keys('Unsaved change')
        self.click_and_wait('#cancel-edit')
        self.assertEqual('Coursework', self.element('.task-title').text)

        self.click_and_wait('.edit-task')
        self.element('#title').clear()
        self.element('#title').send_keys('Reviewed coursework')
        self.click_and_wait('#save-task')
        self.assertEqual('Reviewed coursework', self.element('.task-title').text)
        self.click_and_wait('.complete-task')
        self.assertEqual('Completed', self.element('.badge').text)
        self.assertEqual('1', self.element('#completed-count').text)

        Select(self.element('#filter')).select_by_visible_text('Personal')
        self.click_and_wait('.filter button')
        self.assertIn('No matching tasks', self.element('body').text)
        self.assertEqual('1', self.element('#total-count').text)
        self.driver.get(self.url)
        self.element('#title').send_keys('Overdue task')
        self.click_and_wait('#save-task')
        # Native date entry is locale-dependent, so use normal HTTP form submission
        # for a deterministic fixture; the browser still verifies the rendered UI.
        task_id = self.driver.find_elements(By.CSS_SELECTOR, '.task')[-1].get_attribute('data-task-id')
        from urllib.parse import urlencode
        yesterday = (datetime.now(timezone(timedelta(hours=5, minutes=30))).date() - timedelta(days=1)).isoformat()
        data = urlencode(dict(title='Overdue task', description='', category='Uncategorized', dueDate=yesterday)).encode()
        urllib.request.urlopen(self.url + f'/tasks/{task_id}/edit', data=data).close()
        self.driver.refresh()
        self.assertEqual('Overdue', self.element('.overdue').text)

        self.element('#title').send_keys('   ')
        self.click_and_wait('#save-task')
        self.assertIn('Please enter a task title', self.element('[role=alert]').text)
        self.assertEqual('2', self.element('#total-count').text)
        if os.environ.get('TEST_SCREENSHOT'):
            self.driver.save_screenshot(os.environ['TEST_SCREENSHOT'])

if __name__ == '__main__':
    unittest.main(verbosity=2)
