import { test, expect } from '@playwright/test';

test.describe('Epam website navigation', () => {
    
test('Verify "Explore Our Client Work" and "Client Work" text visibility', async ({ page }) => {

    // Step 1: Navigate to https://www.epam.com/
    await page.goto('https://www.epam.com/');

    // Step 2: Select "Services" from the header menu
    await page.locator('nav >> text=Services').click();

    // Step 3: Click the "Explore Our Client Work" link
    await page.locator('text=Explore Our Client Work').click();

    // Step 4: Verify that the "Client Work" text is visible on the page
    await expect(page.locator('text=Client Work')).toBeVisible();

    });
  
});