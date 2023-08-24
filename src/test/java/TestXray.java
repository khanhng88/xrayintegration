/*
Created by emman at 8/21/2023 4:14 PM
*/

import app.getxray.xray.testng.annotations.XrayTest;
import io.qameta.allure.TmsLink;
import io.qameta.allure.TmsLinks;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({app.getxray.xray.testng.listeners.XrayListener.class})
public class TestXray {

    @BeforeSuite
    public void beforeSuite(){
        System.out.println("before suite");
    }

    @Test
    @XrayTest(key = "AP-48")
    @TmsLinks({@TmsLink("AP-48"), @TmsLink("AP-49")})
    public void test() {
        System.out.println("Khanh test Xray");
    }

    @Test
    @XrayTest(key = "AP-49")
    @TmsLink("AP-49")
    public void test2() {
        System.out.println("Khanh test Xray");
        Assert.assertTrue(false);
    }

    @AfterMethod
    public void afterTest() {
        System.out.println("After test");
    }
}
