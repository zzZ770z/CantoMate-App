package com.example.cantomate.slang.data

import android.content.SharedPreferences
import com.example.cantomate.slang.model.Slang
import java.time.LocalDate

class SlangRepository(
    private val sharedPreferences: SharedPreferences
) {
    private val localSlangPool = listOf(
        Slang(
            slang = "\u505a\u54a9",
            jyutping = "zou6 me1",
            meaning = "why / what are you doing",
            usage = "Informal, very common in daily conversations.",
            example = "\u4f60\u505a\u54a9\u54c1\u9072\u8fd4\uff1f"
        ),
        Slang(
            slang = "\u641e\u6382",
            jyutping = "gaau2 dim6",
            meaning = "to get something done",
            usage = "Used when a task is completed smoothly.",
            example = "\u4efd\u529f\u8ab2\u6211\u6628\u665a\u641e\u6382\u5497\u3002"
        ),
        Slang(
            slang = "\u57cb\u55ae",
            jyutping = "maai4 daan1",
            meaning = "to pay the bill",
            usage = "Commonly used in restaurants and cafes.",
            example = "\u5514\u8a72\uff0c\u57cb\u55ae\u3002"
        ),
        Slang(
            slang = "\u5187\u8a08",
            jyutping = "mou5 gai3",
            meaning = "no choice / can't be helped",
            usage = "Used to express helplessness in a situation.",
            example = "\u843d\u5927\u96e8\uff0c\u5187\u8a08\u5566\u3002"
        ),
        Slang(
            slang = "\u6536\u5de5",
            jyutping = "sau1 gung1",
            meaning = "to finish work",
            usage = "Very common after office hours.",
            example = "\u4ed6\u4eca\u665a\u4e5d\u9ede\u5148\u6536\u5de5\u3002"
        ),
        Slang(
            slang = "hea",
            jyutping = "hia1",
            meaning = "to chill lazily / do nothing productive",
            usage = "Popular colloquial expression among younger speakers.",
            example = "\u653e\u5047\u6211\u60f3\u5728\u5c4b\u4f01hea\u3002"
        ),
        Slang(
            slang = "chur",
            jyutping = "coek3",
            meaning = "intense and exhausting",
            usage = "Describes heavy workload or high pressure.",
            example = "\u4eca\u500b\u79ae\u62dc\u771f\u4fc2\u597dchur\u3002"
        ),
        Slang(
            slang = "\u626e\u5622",
            jyutping = "baan6 je5",
            meaning = "to act pretentious / show off",
            usage = "Used playfully or critically depending on tone.",
            example = "\u4f62\u6210\u65e5\u626e\u5622\u3002"
        ),
        Slang(
            slang = "\u7206seed",
            jyutping = "baau3 si4",
            meaning = "to go all out / use full effort",
            usage = "Common in study, work, and gaming contexts.",
            example = "\u807d\u65e5\u8003\u8a66\uff0c\u4eca\u665a\u8981\u7206seed\u6eab\u66f8\u3002"
        ),
        Slang(
            slang = "\u6709\u51c7\u641e\u932f",
            jyutping = "jau5 mou5 gaau2 co3",
            meaning = "are you serious? / you've got to be kidding",
            usage = "Expresses surprise, frustration, or disbelief.",
            example = "\u53c8\u585e\u8eca\uff1f\u6709\u51c7\u641e\u932f\uff01"
        ),
        Slang(
            slang = "\u98df\u5497\u98ef\u672a",
            jyutping = "sik6 zo2 faan6 mei6",
            meaning = "have you eaten?",
            usage = "Friendly greeting among Cantonese speakers.",
            example = "\u55c2\uff0c\u98df\u5497\u98ef\u672a\u5440\uff1f"
        ),
        Slang(
            slang = "\u6382",
            jyutping = "dim6",
            meaning = "great / works well / okay",
            usage = "Short positive response in daily speech.",
            example = "\u5462\u500b\u65b9\u6cd5\u597d\u6382\uff01"
        ),
        Slang(
            slang = "\u5514\u8a72\u6652",
            jyutping = "m4 goi1 saai3",
            meaning = "thank you very much",
            usage = "Polite phrase used for favors and service.",
            example = "\u5e6b\u6211\u624b\u5148\uff0c\u5514\u8a72\u6652\u3002"
        ),
        Slang(
            slang = "\u8d95\u982d\u8d95\u547d",
            jyutping = "gon2 tau4 gon2 meng6",
            meaning = "to rush like crazy",
            usage = "Describes being in a severe hurry.",
            example = "\u65e9\u6668\u8d95\u982d\u8d95\u547d\u8fd4\u5de5\u3002"
        ),
        Slang(
            slang = "\u4f01\u7406",
            jyutping = "kei5 lei5",
            meaning = "neat / well-organized / presentable",
            usage = "Used for appearance, rooms, and arrangement.",
            example = "\u4f60\u4eca\u65e5\u8457\u5f97\u597d\u4f01\u7406\u5594\u3002"
        )
    )

    fun getSlangOfToday(): Slang {
        val today = LocalDate.now()
        val todayKey = today.toString()
        val cachedDay = sharedPreferences.getString(KEY_LAST_DATE, null)
        val cachedIndex = sharedPreferences.getInt(KEY_LAST_INDEX, INVALID_INDEX)

        if (cachedDay == todayKey && cachedIndex in localSlangPool.indices) {
            return localSlangPool[cachedIndex]
        }

        val newIndex = Math.floorMod(today.toEpochDay().toInt(), localSlangPool.size)
        sharedPreferences.edit()
            .putString(KEY_LAST_DATE, todayKey)
            .putInt(KEY_LAST_INDEX, newIndex)
            .apply()

        return localSlangPool[newIndex]
    }

    // TODO: Optional enhancement - fetch one slang from DeepSeek and cache by date.
    fun getLocalPoolSize(): Int = localSlangPool.size

    companion object {
        const val PREF_NAME = "slang_daily_pref"
        private const val KEY_LAST_DATE = "key_last_date"
        private const val KEY_LAST_INDEX = "key_last_index"
        private const val INVALID_INDEX = -1
    }
}
