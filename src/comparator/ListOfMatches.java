/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comparator;

import java.io.File;
import java.util.HashMap;

/**
 *
 * @author dkosyakov
 */
public class ListOfMatches {
    
    private final File file1, file2;
    private final Integer[] mainList;
    public static final HashMap<Integer,String> returnCodes=new HashMap();

        /*коды позитивные - соответствия / негативные - несоответствия, 
    0-100 - пары "ключ-значение", 101-200 xml несоответствие*/
    public static final int RETURN_CODE_PAIRS_MATCH=1;
    public static final int RETURN_CODE_FULL_EQUIVALENCE=2;
    public static final int RETURN_CODE_XML_MATCH=101;
    public static final int RETURN_CODE_REACHED_EOF_IN_FILE_1=-1;
    public static final int RETURN_CODE_REACHED_EOF_IN_FILE_2=-2;
    public static final int RETURN_CODE_SYNTAX_MISMATCH=-3;
    public static final int RETURN_CODE_DIFFERENT_NUMBER_OF_PAIRS=-4;
    public static final int RETURN_CODE_KEYS_NOT_EQUAL=-5;
    public static final int RETURN_CODE_STRING_DATA_NOT_EQUAL=-6;
    public static final int RETURN_CODE_NUMBERS_ARRAYS_OF_DIFFERENT_LENGTH=-7;
    public static final int RETURN_CODE_NUMBERS_NOT_EQUAL=-8;
    public static final int RETURN_CODE_XML_DIFFERENT_NUMBER_OF_ELEMENTS=-102;
    public static final int RETURN_CODE_XML_DIFFERENT_ELEMENTS=-103;
    public static final int RETURN_CODE_XML_DIFFERENT_ATTRIBUTES=-104;
    public static final int RETURN_CODE_XML_INVALID=-105;

    
    ListOfMatches(File file1, File file2, Integer[] listOfMatches){ 
    /*конструктора по умолчанию не существует, экземпляры класса создаются путём вызова 
        метода FileComparator.compare 
    */
        this.file1=file1;
        this.file2=file2;
        this.mainList=listOfMatches;
        
        //здесь будут текстовые комментарии к различным видам соответствия / несоответствия
        returnCodes.put(RETURN_CODE_PAIRS_MATCH, "Lines match (key-value pairs)");
        returnCodes.put(RETURN_CODE_FULL_EQUIVALENCE, "Lines are equal");
        returnCodes.put(RETURN_CODE_XML_MATCH, "Lines match (XML node)");
        returnCodes.put(RETURN_CODE_REACHED_EOF_IN_FILE_1, "The second file is longer than the first one");
        returnCodes.put(RETURN_CODE_REACHED_EOF_IN_FILE_2, "Line exists only in the first file");
        returnCodes.put(RETURN_CODE_SYNTAX_MISMATCH, "Line does not meet the key-value set data structure");
        returnCodes.put(RETURN_CODE_DIFFERENT_NUMBER_OF_PAIRS, "Lines differ in number of pairs");
        returnCodes.put(RETURN_CODE_KEYS_NOT_EQUAL, "Lines differ in set of keys");
        returnCodes.put(RETURN_CODE_STRING_DATA_NOT_EQUAL, "Lines differ in string values");
        returnCodes.put(RETURN_CODE_NUMBERS_ARRAYS_OF_DIFFERENT_LENGTH, "Lines differ in length of  values arrays");
        returnCodes.put(RETURN_CODE_NUMBERS_NOT_EQUAL, "Lines differ in numeric data");
        returnCodes.put(RETURN_CODE_XML_DIFFERENT_NUMBER_OF_ELEMENTS, "Lines differ in number of XML elements");
        returnCodes.put(RETURN_CODE_XML_DIFFERENT_ELEMENTS, "Lines differ in XML elements");
        returnCodes.put(RETURN_CODE_XML_DIFFERENT_ATTRIBUTES, "Lines differ in attributes of XML elements");
        returnCodes.put(RETURN_CODE_XML_INVALID, "Line contains invalid XML");
    }
    
    public boolean linesMatch(int lineNumber){
        if (lineNumber<mainList.length) {
            return mainList[lineNumber]>0;
        }
        return false;
    }
    
    public int getMatchStatus(int lineNumber){
        return mainList[lineNumber];
    }
    
    public String getMatchStatusString(int lineNumber){
        return returnCodes.get(lineNumber);
    }
    
    public Integer[] getAll(){
        return mainList;
    }
    
    public void printMismathes(){
        boolean filesEqual=true;
        System.out.println("Mismatches in files");
        System.out.println("File №1 : "+file1.getName());
        System.out.println("File №2 : "+file2.getName());
        System.out.println("------");
        for (int i=0;i<mainList.length;i++){
            int retCode=mainList[i];
            if (retCode<0){
               System.out.println("Line №"+i+" : "+returnCodes.get(retCode));
               filesEqual=false;
            }   
        }
        if (filesEqual) System.out.println("Files equal");
        
    }

    
}

