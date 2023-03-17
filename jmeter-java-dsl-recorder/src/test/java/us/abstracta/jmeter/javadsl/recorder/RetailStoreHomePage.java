package us.abstracta.jmeter.javadsl.recorder;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RetailStoreHomePage {

  private final WebDriver driver;

  public RetailStoreHomePage(WebDriver driver) {
    this.driver = driver;
  }

  public void addFirstProductToCart() {
    WebElement firstProduct = findProducts().get(0);
    WebElement cartButton = findProductCartButton(firstProduct);
    clickButton(cartButton);
  }

  private List<WebElement> findProducts() {
    By productsLocator = By.cssSelector(".product");
    new WebDriverWait(driver, Duration.ofSeconds(10))
        .until(ExpectedConditions.numberOfElementsToBeMoreThan(productsLocator, 0));
    return driver.findElements(productsLocator);
  }


  private WebElement findProductCartButton(WebElement product) {
    return product.findElement(By.xpath("//a[contains(.,'Add to cart')]"));
  }

  private void clickButton(WebElement button) {
    /*
     for some reason can't use element.click because we get: Element is not clickable at point
     (x, y). So using javascript is a way to make it work
     */
    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", button);
  }

}
