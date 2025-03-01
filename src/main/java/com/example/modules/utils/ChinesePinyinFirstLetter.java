package com.example.modules.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.junit.jupiter.api.Test;


public class ChinesePinyinFirstLetter {
    public static char getFirstLetterOfFirstCharacter(String chineseString) {
        if (chineseString == null || chineseString.isEmpty()) {
            return ' '; // 空或 null 返回空格
        }

        char firstChar = chineseString.charAt(0);

        // 检查是否是中文字符
        if (Character.toString(firstChar).matches("[\\u4E00-\\u9FA5]")) {
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

            try {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(firstChar, format);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    // 取拼音首字母并大写
                    return Character.toUpperCase(pinyinArray[0].charAt(0));
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        } else if (Character.isLetter(firstChar)) {
            // 如果是字母，直接返回大写形式
            return Character.toUpperCase(firstChar);
        }

        // 非中文且非字母时返回空字符（或可调整为默认值）
        return '\0';
    }
    @Test
    public  void testGetFirstLetterOfFirstCharacter() {
        String chineseString = "xxx";
        char firstLetter = getFirstLetterOfFirstCharacter(chineseString);
        System.out.println("The first letter of the first character in '" + chineseString + "' is: " + firstLetter);
    }
}
