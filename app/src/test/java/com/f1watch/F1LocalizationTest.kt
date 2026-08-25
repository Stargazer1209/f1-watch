package com.f1watch

import com.f1watch.util.F1Localization
import org.junit.Assert.assertEquals
import org.junit.Test

class F1LocalizationTest {

    @Test
    fun testGrandPrixTranslations() {
        assertEquals("意大利大奖赛", F1Localization.getRaceName("Italian Grand Prix"))
        assertEquals("澳大利亚大奖赛", F1Localization.getRaceName("Australian Grand Prix"))
        assertEquals("中国大奖赛", F1Localization.getRaceName("Chinese Grand Prix"))
        assertEquals("摩纳哥大奖赛", F1Localization.getRaceName("Monaco Grand Prix"))
        assertEquals("巴塞罗那大奖赛", F1Localization.getRaceName("Barcelona Grand Prix"))
        assertEquals("马来西亚大奖赛", F1Localization.getRaceName("Bahrain Grand Prix in Malaysia"))
    }

    @Test
    fun testCircuitTranslations() {
        assertEquals("阿尔伯特公园赛道", F1Localization.getCircuitName("Albert Park Grand Prix Circuit"))
        assertEquals("阿尔伯特公园赛道", F1Localization.getCircuitName("Albert Park Circuit"))
        assertEquals("蒙扎国家赛车场", F1Localization.getCircuitName("Autodromo Nazionale di Monza"))
        assertEquals("蒙扎国家赛车场", F1Localization.getCircuitName("Autodromo Nazionale Monza"))
        assertEquals("赞德福特赛道", F1Localization.getCircuitName("Circuit Park Zandvoort"))
        assertEquals("上海国际赛车场", F1Localization.getCircuitName("Shanghai International Circuit"))
    }

    @Test
    fun testLocalityAndCountryTranslations() {
        assertEquals("墨尔本", F1Localization.getLocality("Melbourne"))
        assertEquals("蒙扎", F1Localization.getLocality("Monza"))
        assertEquals("上海", F1Localization.getLocality("Shanghai"))
        assertEquals("意大利", F1Localization.getCountry("Italy"))
        assertEquals("澳大利亚", F1Localization.getCountry("Australia"))
        assertEquals("中国", F1Localization.getCountry("China"))
    }

    @Test
    fun testDriverNamesTranslations() {
        assertEquals("乔治·拉塞尔", F1Localization.getDriverName("George Russell"))
        assertEquals("刘易斯·汉密尔顿", F1Localization.getDriverName("Lewis Hamilton"))
        assertEquals("基米·安东内利", F1Localization.getDriverName("Andrea Kimi Antonelli"))
        assertEquals("阿尔维德·林德布拉德", F1Localization.getDriverName("Arvid Lindblad"))
        assertEquals("塞尔吉奥·佩雷兹", F1Localization.getDriverName("Sergio Perez"))
        assertEquals("周冠宇", F1Localization.getDriverName("Zhou Guanyu"))
    }

    @Test
    fun testConstructorNamesTranslations() {
        assertEquals("法拉利", F1Localization.getConstructorName("Ferrari"))
        assertEquals("梅赛德斯", F1Localization.getConstructorName("Mercedes"))
        assertEquals("红牛", F1Localization.getConstructorName("Red Bull"))
        assertEquals("阿尔派", F1Localization.getConstructorName("Alpine F1 Team"))
        assertEquals("阿尔派", F1Localization.getConstructorName("Alpine"))
        assertEquals("小牛 RB", F1Localization.getConstructorName("RB F1 Team"))
        assertEquals("小牛 RB", F1Localization.getConstructorName("Visa Cash App RB"))
        assertEquals("奥迪", F1Localization.getConstructorName("Audi F1 Team"))
        assertEquals("凯迪拉克", F1Localization.getConstructorName("Cadillac F1 Team"))
        assertEquals("凯迪拉克", F1Localization.getConstructorName("Andretti Cadillac"))
    }
}
