/*
Created by emman at 8/18/2023 7:10 PM
*/

import app.getxray.xray.testng.annotations.Requirement;
import app.getxray.xray.testng.annotations.XrayTest;
import io.qameta.allure.TmsLink;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({app.getxray.xray.testng.listeners.XrayListener.class})
public class TestXray {

    @Test
    @XrayTest(key = "AP-48")
    @TmsLink("AP-48")
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
}
