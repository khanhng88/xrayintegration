/*
Created by emman at 8/21/2023 4:14 PM
*/

import app.getxray.xray.testng.annotations.XrayTest;
import io.qameta.allure.Step;
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
    @XrayTest(key = "AP-48,AP-49, AP-53,")
    public void test() {
        System.out.println("Khanh test Xray");
        step_1();
    }

    @Test
    @XrayTest(key = " AP-59")
    public void test2() {
        System.out.println("Khanh test Xray");
        step_2();
        step_3();
    }

    @AfterMethod
    public void afterTest() {
        System.out.println("After test");
    }

    @Step("Do step 1")
    public void step_1() {
        System.out.println("Do step 1");
    }

    @Step("Do step 2")
    public void step_2() {
        System.out.println("Do step 2");
    }

    @Step("Do step 3")
    public void step_3() {
        System.out.println("Do step 3");
        Assert.assertTrue(false);
    }
}
