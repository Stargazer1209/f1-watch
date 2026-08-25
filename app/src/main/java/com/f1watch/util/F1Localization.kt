package com.f1watch.util

import java.util.Locale

object F1Localization {

    val isChinese: Boolean
        get() {
            val lang = Locale.getDefault().language
            return lang.startsWith("zh", ignoreCase = true)
        }

    fun getRaceName(raceName: String): String {
        if (!isChinese) return raceName
        val lower = raceName.trim().lowercase()
        return when {
            "australian" in lower -> "澳大利亚大奖赛"
            "chinese" in lower -> "中国大奖赛"
            "japanese" in lower -> "日本大奖赛"
            "bahrain" in lower && "malaysia" in lower -> "马来西亚大奖赛"
            "bahrain" in lower -> "巴林大奖赛"
            "saudi" in lower -> "沙特阿拉伯大奖赛"
            "miami" in lower -> "迈阿密大奖赛"
            "emilia" in lower || "imola" in lower -> "艾米利亚-罗马涅大奖赛"
            "monaco" in lower -> "摩纳哥大奖赛"
            "barcelona" in lower -> "巴塞罗那大奖赛"
            "spanish" in lower -> "西班牙大奖赛"
            "canadian" in lower -> "加拿大大奖赛"
            "austrian" in lower -> "奥地利大奖赛"
            "british" in lower -> "英国大奖赛"
            "hungarian" in lower -> "匈牙利大奖赛"
            "belgian" in lower -> "比利时大奖赛"
            "dutch" in lower -> "荷兰大奖赛"
            "italian" in lower -> "意大利大奖赛"
            "azerbaijan" in lower || "baku" in lower -> "阿塞拜疆大奖赛"
            "malaysian" in lower || "malaysia" in lower -> "马来西亚大奖赛"
            "singapore" in lower -> "新加坡大奖赛"
            "united states" in lower || "usa" in lower || "austin" in lower -> "美国大奖赛"
            "mexico" in lower -> "墨西哥城大奖赛"
            "são paulo" in lower || "sao paulo" in lower || "brazilian" in lower -> "圣保罗大奖赛"
            "las vegas" in lower || "vegas" in lower -> "拉斯维加斯大奖赛"
            "qatar" in lower -> "卡塔尔大奖赛"
            "abu dhabi" in lower -> "阿布扎比大奖赛"
            else -> raceName
        }
    }

    fun getCircuitName(circuitName: String): String {
        if (!isChinese) return circuitName
        val lower = circuitName.trim().lowercase()
        return when {
            "albert park" in lower || "melbourne" in lower -> "阿尔伯特公园赛道"
            "shanghai" in lower -> "上海国际赛车场"
            "suzuka" in lower -> "铃鹿国际赛道"
            "bahrain" in lower || "sakhir" in lower -> "萨基尔巴林国际赛车场"
            "jeddah" in lower -> "吉达滨海街道赛道"
            "miami" in lower -> "迈阿密国际赛车场"
            "imola" in lower || "enzo" in lower -> "伊莫拉恩佐与迪诺·法拉利赛道"
            "monaco" in lower || "monte carlo" in lower -> "摩纳哥蒙特卡洛街道赛道"
            "catalunya" in lower || "barcelona" in lower -> "巴塞罗那加泰罗尼亚赛道"
            "villeneuve" in lower || "montreal" in lower -> "蒙特利尔吉尔·维伦纽夫赛道"
            "red bull ring" in lower || "spielberg" in lower -> "斯皮尔伯格红牛环赛道"
            "silverstone" in lower -> "银石赛道"
            "hungaroring" in lower || "budapest" in lower -> "亨格罗宁赛道"
            "spa" in lower || "francorchamps" in lower -> "斯帕-弗朗科尔尚赛道"
            "zandvoort" in lower -> "赞德福特赛道"
            "monza" in lower -> "蒙扎国家赛车场"
            "madrid" in lower || "madring" in lower -> "马德里街道赛道"
            "baku" in lower -> "巴库城市赛道"
            "sepang" in lower -> "雪邦国际赛道"
            "marina bay" in lower || "singapore" in lower -> "滨海湾市街赛道"
            "americas" in lower || "cota" in lower || "austin" in lower -> "奥斯汀美洲赛道"
            "rodriguez" in lower || "hermanos" in lower -> "罗德里格斯兄弟赛道"
            "interlagos" in lower || "pace" in lower || "são paulo" in lower || "sao paulo" in lower -> "英特拉格斯若泽·卡洛斯·帕塞赛道"
            "las vegas" in lower || "vegas" in lower -> "拉斯维加斯街道赛道"
            "losail" in lower || "lusail" in lower || "qatar" in lower -> "罗赛尔国际赛车场"
            "yas marina" in lower || "abu dhabi" in lower -> "亚斯码头赛道"
            else -> circuitName
        }
    }

    fun getLocality(locality: String): String {
        if (!isChinese) return locality
        val lower = locality.trim().lowercase()
        return when {
            "melbourne" in lower -> "墨尔本"
            "shanghai" in lower -> "上海"
            "suzuka" in lower -> "铃鹿"
            "sakhir" in lower -> "萨基尔"
            "jeddah" in lower -> "吉达"
            "miami" in lower -> "迈阿密"
            "imola" in lower -> "伊莫拉"
            "monte carlo" in lower || "monaco" in lower -> "蒙特卡洛"
            "barcelona" in lower || "montmel" in lower -> "巴塞罗那"
            "montreal" in lower || "montréal" in lower -> "蒙特利尔"
            "spielberg" in lower -> "斯皮尔伯格"
            "silverstone" in lower -> "银石"
            "budapest" in lower -> "布达佩斯"
            "spa" in lower || "francorchamps" in lower -> "斯帕"
            "zandvoort" in lower -> "赞德福特"
            "monza" in lower -> "蒙扎"
            "madrid" in lower -> "马德里"
            "baku" in lower -> "巴库"
            "kuala lumpur" in lower || "sepang" in lower -> "吉隆坡"
            "singapore" in lower || "marina bay" in lower -> "新加坡"
            "austin" in lower -> "奥斯汀"
            "mexico" in lower -> "墨西哥城"
            "são paulo" in lower || "sao paulo" in lower -> "圣保罗"
            "las vegas" in lower || "vegas" in lower -> "拉斯维加斯"
            "lusail" in lower || "losail" in lower || "al daayen" in lower -> "罗赛尔"
            "abu dhabi" in lower || "yas island" in lower -> "阿布扎比"
            else -> locality
        }
    }

    fun getCountry(country: String): String {
        if (!isChinese) return country
        val lower = country.trim().lowercase()
        return when {
            "australia" in lower -> "澳大利亚"
            "china" in lower -> "中国"
            "japan" in lower -> "日本"
            "bahrain" in lower -> "巴林"
            "saudi" in lower -> "沙特阿拉伯"
            "usa" in lower || "united states" in lower || "american" in lower -> "美国"
            "canada" in lower || "canadian" in lower -> "加拿大"
            "monaco" in lower || "monegasque" in lower -> "摩纳哥"
            "spain" in lower || "spanish" in lower -> "西班牙"
            "austria" in lower || "austrian" in lower -> "奥地利"
            "uk" in lower || "great britain" in lower || "british" in lower || "united kingdom" in lower -> "英国"
            "hungary" in lower || "hungarian" in lower -> "匈牙利"
            "belgium" in lower || "belgian" in lower -> "比利时"
            "netherlands" in lower || "dutch" in lower -> "荷兰"
            "italy" in lower || "italian" in lower -> "意大利"
            "azerbaijan" in lower -> "阿塞拜疆"
            "malaysia" in lower || "malaysian" in lower -> "马来西亚"
            "singapore" in lower -> "新加坡"
            "mexico" in lower || "mexican" in lower -> "墨西哥"
            "brazil" in lower || "brazilian" in lower -> "巴西"
            "qatar" in lower -> "卡塔尔"
            "uae" in lower || "emirates" in lower -> "阿联酋"
            "germany" in lower || "german" in lower -> "德国"
            "france" in lower || "french" in lower -> "法国"
            "switzerland" in lower || "swiss" in lower -> "瑞士"
            "new zealand" in lower -> "新西兰"
            "argentina" in lower || "argentine" in lower -> "阿根廷"
            "thailand" in lower || "thai" in lower -> "泰国"
            "finland" in lower || "finnish" in lower -> "芬兰"
            "denmark" in lower || "danish" in lower -> "丹麦"
            else -> country
        }
    }

    fun getDriverName(fullName: String): String {
        if (!isChinese) return fullName
        val lower = fullName.trim().lowercase()
        return when {
            "russell" in lower -> "乔治·拉塞尔"
            "hamilton" in lower -> "刘易斯·汉密尔顿"
            "verstappen" in lower -> "马克斯·维斯塔潘"
            "norris" in lower -> "兰多·诺里斯"
            "leclerc" in lower -> "夏尔·勒克莱尔"
            "piastri" in lower -> "奥斯卡·皮亚斯特里"
            "sainz" in lower -> "卡洛斯·塞恩斯"
            "alonso" in lower -> "费尔南多·阿隆索"
            "antonelli" in lower || "andrea kimi" in lower -> "基米·安东内利"
            "perez" in lower || "pérez" in lower -> "塞尔吉奥·佩雷兹"
            "lawson" in lower -> "利亚姆·劳森"
            "bearman" in lower -> "奥利弗·比尔曼"
            "colapinto" in lower -> "弗朗哥·科拉平托"
            "bottas" in lower -> "瓦尔特利·博塔斯"
            "zhou" in lower || "guanyu" in lower -> "周冠宇"
            "gasly" in lower -> "皮埃尔·加斯利"
            "ocon" in lower -> "埃斯特班·奥康"
            "albon" in lower -> "亚历山大·阿尔本"
            "tsunoda" in lower -> "角田裕毅"
            "hulkenberg" in lower || "hülkenberg" in lower -> "尼科·霍肯伯格"
            "magnussen" in lower -> "凯文·马格努森"
            "stroll" in lower -> "兰斯·斯托尔"
            "doohan" in lower -> "杰克·杜汉"
            "bortoleto" in lower -> "加布里埃尔·博托莱托"
            "hadjar" in lower -> "伊萨克·哈贾尔"
            "lindblad" in lower || "arvid" in lower -> "阿尔维德·林德布拉德"
            "ricciardo" in lower -> "丹尼尔·里卡多"
            else -> fullName
        }
    }

    fun getConstructorName(name: String): String {
        if (!isChinese) return name
        val lower = name.trim().lowercase()
        return when {
            "ferrari" in lower -> "法拉利"
            "mclaren" in lower -> "迈凯伦"
            "red bull" in lower -> "红牛"
            "mercedes" in lower -> "梅赛德斯"
            "aston martin" in lower -> "阿斯顿·马丁"
            "williams" in lower -> "威廉姆斯"
            "alpine" in lower -> "阿尔派"
            "sauber" in lower || "kick" in lower -> "索伯"
            "audi" in lower -> "奥迪"
            "rb" in lower || "racing bulls" in lower || "alphatauri" in lower || "toro rosso" in lower -> "小牛 RB"
            "haas" in lower -> "哈斯"
            "cadillac" in lower || "andretti" in lower -> "凯迪拉克"
            else -> name
        }
    }

    fun getSessionName(sessionName: String): String {
        if (!isChinese) return sessionName
        return when (sessionName) {
            "First Practice", "Practice 1", "FP1" -> "第一节自由练习 (FP1)"
            "Second Practice", "Practice 2", "FP2" -> "第二节自由练习 (FP2)"
            "Third Practice", "Practice 3", "FP3" -> "第三节自由练习 (FP3)"
            "Sprint Qualifying", "Sprint Shootout" -> "冲刺排位赛"
            "Sprint" -> "冲刺正赛"
            "Qualifying", "Q" -> "大奖赛排位赛"
            "Race" -> "大奖赛正赛"
            else -> sessionName
        }
    }
}
