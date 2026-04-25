package com.example.cantomate.jyutping

data class JyutpingItem(
    val code: String,
    val exampleChar: String,
    val exampleWord: String,
    val mouthTip: String
)

val jyutpingInitials = listOf(
    JyutpingItem("b", "波", "波士", "双唇紧闭，爆破送气，声带不振动"),
    JyutpingItem("p", "坡", "啤酒", "双唇紧闭，强爆破送气，声带不振动"),
    JyutpingItem("m", "摸", "妈咪", "双唇紧闭，鼻音，声带振动"),
    JyutpingItem("f", "花", "飞机", "上齿触下唇，摩擦送气，声带不振动"),
    JyutpingItem("d", "多", "地铁", "舌尖抵上齿龈，爆破送气，声带不振动"),
    JyutpingItem("t", "拖", "天台", "舌尖抵上齿龈，强爆破送气，声带不振动"),
    JyutpingItem("n", "挪", "女人", "舌尖抵上齿龈，鼻音，声带振动"),
    JyutpingItem("l", "啰", "垃圾", "舌尖抵上齿龈，边音，声带振动"),
    JyutpingItem("g", "家", "香港", "舌根抵软腭，爆破送气，声带不振动"),
    JyutpingItem("k", "卡", "开心", "舌根抵软腭，强爆破送气，声带不振动"),
    JyutpingItem("ng", "我", "银行", "舌根抵软腭，鼻音，声带振动"),
    JyutpingItem("h", "哈", "虾饺", "舌根轻触软腭，摩擦送气，声带不振动"),
    JyutpingItem("w", "蛙", "旺角", "双唇收圆，浊半元音，声带振动"),
    JyutpingItem("z", "左", "早餐", "舌尖抵下齿，先塞后擦，声带不振动"),
    JyutpingItem("c", "初", "茶餐厅", "舌尖抵下齿，先塞后强擦，声带不振动"),
    JyutpingItem("s", "疏", "街市", "舌尖抵下齿，摩擦送气，声带不振动"),
    JyutpingItem("j", "衣", "依然", "舌尖抵下齿，浊半元音，声带振动"),
    JyutpingItem("gw", "瓜", "广九", "圆唇舌根音，爆破送气，声带不振动"),
    JyutpingItem("kw", "夸", "邝美云", "圆唇舌根音，强爆破送气，声带不振动")
)


val jyutpingFinals = listOf(
    JyutpingItem("aa", "啊", "爸爸", "开口大，长元音，口型不变"),
    JyutpingItem("aai", "挨", "新界", "aa+i，长元音，口型由大到小"),
    JyutpingItem("aau", "坳", "澳洲", "aa+u，长元音，口型由大到圆"),
    JyutpingItem("aam", "庵", "点心", "aa+m，长元音，双唇闭拢鼻音收尾"),
    JyutpingItem("aan", "晏", "平安", "aa+n，长元音，舌尖抵上齿龈鼻音收尾"),
    JyutpingItem("aang", "莺", "香港", "aa+ng，长元音，舌根抵软腭鼻音收尾"),
    JyutpingItem("i", "衣", "时间", "长元音，舌尖抵下齿，嘴角向两边拉开"),
    JyutpingItem("u", "乌", "功夫", "长元音，双唇收圆，舌根后缩"),
    JyutpingItem("yu", "于", "粤语", "长元音，舌尖抵下齿，圆唇"),
    JyutpingItem("m", "唔", "唔该", "鼻音独立韵，双唇闭拢，声带振动"),
    JyutpingItem("ng", "吴", "吴先生", "鼻音独立韵，舌根抵软腭，声带振动")
)

val jyutpingTones = listOf(
    JyutpingItem("1", "诗", "si1 诗", "阴平，高平调，55调值，音高且平稳"),
    JyutpingItem("2", "史", "si2 史", "阴上，高升调，35调值，从中音升到高音"),
    JyutpingItem("3", "试", "si3 试", "阴去，中平调，33调值，中音平稳不变"),
    JyutpingItem("4", "时", "si4 时", "阳平，低降调，21调值，从低往下降"),
    JyutpingItem("5", "市", "si5 市", "阳上，低升调，23调值，从低音升到中音"),
    JyutpingItem("6", "是", "si6 是", "阳去，低平调，22调值，低音平稳不变")
)