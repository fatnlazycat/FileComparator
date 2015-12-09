package comparator;

import static comparator.ListOfMatches.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import static java.lang.Double.parseDouble;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/*
 * @author dkosyakov
Класс FileComparator служит для сравнения файлов, имеющих структуру согласно условию задания.
Для этого существует метод 
public ListOfMatches compare(File file1, File file2)
Аргументы file1 и file2 - это файлы, которые требуется сравнить
Возвращается экземпляр класса ListOfMatches, который является вспомогательным и имеет ряд полезных методов.
Кроме того, этот класс содержит коды вариантов равенства/неравенства строк, и их текстовое описание
 */
public class FileComparator {

private final String[] technicalEmptyArray=new String[0]; 

private LinkedHashMap<String,String[]> parseLineAsXML(String a) throws SAXException {
    
    /*объявляем переменную LinkedHashMap<String,String[]>, к которой будет собираться возвращаемое
    методом значение.
    Ключ String - имя xml-узла. Значение String[] - массив атрибутов*/
    LinkedHashMap<String,String[]> result=new LinkedHashMap<>();
    
    try {
        XMLReader xr=XMLReaderFactory.createXMLReader();
        DefaultHandler handler=new DefaultHandler(){
            
            public void characters (char ch[], int start, int length){
                StringBuilder sb=new StringBuilder();
                for (int i = start; i < start + length; i++) {
                    sb.append(ch[i]);
                }
                result.put(sb.toString(), technicalEmptyArray);
            }
            
            public void startElement (String uri, String name, String qName, Attributes atts){
                int numberOfAtts=atts.getLength();
                String[] attsArray;
                if (numberOfAtts==0) attsArray=technicalEmptyArray;
                    else attsArray=new String[numberOfAtts];
                for (int i=0; i<numberOfAtts; i++){
                    attsArray[i]=atts.getQName(i)+"="+atts.getValue(i);
                }
                Arrays.sort(attsArray); /*атрибуты сортируются для последующего сравнения,
                 т.к. xml-узлы одного имени с одинаковыми атрибутами,
                 объявленными в разном порядке, идентичны*/
                result.put(qName, attsArray);
            }
            
            public void endElement (String uri, String name, String qName){
                result.put(">"+qName, technicalEmptyArray);
            }
        };
        
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);
        xr.parse(new InputSource(new StringReader(a)));
    } catch(IOException e) {
        System.out.println(e);
    };
    return result;
}

private int comparePairs(String str1, String str2){
    
    if (str1.equals(str2)){return RETURN_CODE_FULL_EQUIVALENCE;} //full equivalence
    
    //проверяем строку на соответствие заявленному синтаксису
    String pattern1 = "\\w+=\\w+(\\.\\w+)?(:\\w+(\\.\\w+)?)?(,\\w+(\\.\\w+)?(:\\w+(\\.\\w+)?)?)"
            + "*(&\\w+=\\w+(\\.\\w+)?(:\\w+(\\.\\w+)?)?(,\\w+(\\.\\w+)?(:\\w+(\\.\\w+)?)?)*)*";
    if (!str1.matches(pattern1)){ return RETURN_CODE_SYNTAX_MISMATCH;}
    
    //теперь разобъём по парам "ключ-значение" (разделитель "&")
    String[] pairs1=str1.split("&");
    String[] pairs2=str2.split("&");
    
    if (!(pairs1.length==pairs2.length)){return RETURN_CODE_DIFFERENT_NUMBER_OF_PAIRS;}
    
    //данный шаблон будет нужен для проверки, имеем ли мы дело со строковыми данными, либо с числовыми
    String pattern2="\\d+(\\.\\d+)?(:\\d+(\\.\\d+)?)?(,\\d+(\\.\\d+)?(:\\d+(\\.\\d+)?)?)*";
    
    for (int i=0; i<pairs1.length; i++){
        
        //отделяем ключи от значений
        String[] currentPair1=pairs1[i].split("=");
        String[] currentPair2=pairs2[i].split("=");
        
        if (!(currentPair1[0].equals(currentPair2[0]))) {return RETURN_CODE_KEYS_NOT_EQUAL;}//не равны ключи
        
        if(!currentPair1[1].matches(pattern2)) { //имеем дело со строковыми данными
            if (!currentPair1[1].equals(currentPair2[1]))  {
                //значения не идентичны, нужно проверять на равенство массивов
                String[] values1Array=currentPair1[1].split(",");
                Arrays.sort(values1Array);
                String[] values2Array=currentPair2[1].split(",");
                Arrays.sort(values2Array);
                if (!(Arrays.equals(values1Array, values2Array)))
                    {return RETURN_CODE_STRING_DATA_NOT_EQUAL;}//неравенство массивов
            }
        
        } else {//имеем дело с цифровыми данными
            
            //создаём технические массивы, нужны будут для преобразования данных в числа
            String[] tech1=currentPair1[1].split(",");
            String[] tech2=currentPair2[1].split(",");
            
            int len=tech1.length;
            if (!(len==tech2.length))
                {return RETURN_CODE_NUMBERS_ARRAYS_OF_DIFFERENT_LENGTH;}//массивы разной длины, точно не равны
            
            //двухмерный массив заполняем данными "ключ-значение" второго порядка (разделитель ":")
            String[][] atLast1=new String[len][2];
            String[][] atLast2=new String[len][2];
            boolean valuesEqualWithoutParsing=true;
            for (int i2=0; i2<len; i2++){
                if (!(tech1[i2].equals(tech2[i2]))) valuesEqualWithoutParsing=false;
                if (!tech1[i2].contains(":")) {
                    atLast1[i2][0]=tech1[i2];
                    atLast1[i2][1]="0";
                } else atLast1[i2]=tech1[i2].split(":", 2);
                if (!tech2[i2].contains(":")) {
                    atLast2[i2][0]=tech2[i2];
                    atLast2[i2][1]="0";
                } else atLast2[i2]=tech2[i2].split(":", 2);
            }
            if (valuesEqualWithoutParsing) {continue;}//цифровые данные равны без преобразования в числа, на основе сравнения строк

            //если до сих пор равенство не обнаружено, преобразуем данные в числа
            double[][] atLast1double=new double[len][2];
            double[][] atLast2double=new double[len][2];
            for (int i2=0; i2<len; i2++){
                atLast1double[i2][0]=parseDouble(atLast1[i2][0]);
                atLast1double[i2][1]=parseDouble(atLast1[i2][1]);
                atLast2double[i2][0]=parseDouble(atLast2[i2][0]);
                atLast2double[i2][1]=parseDouble(atLast2[i2][1]);               
            }
            
            //пузырьковая сортировка
            boolean somethingElseToSort=!(len==1);
            //сортируем данные из первого файла
            while (somethingElseToSort){
                somethingElseToSort=false;
                for (int i2=0; i2<len-1; i2++){
                    if ((atLast1double[i2][0]>atLast1double[i2+1][0])||
                            ((atLast1double[i2][0]==atLast1double[i2+1][0])&&
                            (atLast1double[i2][1]>atLast1double[i2+1][1]))){
                        double techDouble1=atLast1double[i2][0];
                        double techDouble2=atLast1double[i2][1];
                        atLast1double[i2][0]=atLast1double[i2+1][0];
                        atLast1double[i2][1]=atLast1double[i2+1][1];
                        atLast1double[i2+1][0]=techDouble1;
                        atLast1double[i2+1][1]=techDouble2;
                        somethingElseToSort=true;
                    }
                }
            }
            somethingElseToSort=!(len==1);
            //сортируем данные из второго файла
            while (somethingElseToSort){
                somethingElseToSort=false;
                for (int i2=0; i2<len-1; i2++){
                    if ((atLast2double[i2][0]>atLast2double[i2+1][0])||
                            ((atLast2double[i2][0]==atLast2double[i2+1][0])&&
                            (atLast2double[i2][1]>atLast2double[i2+1][1]))){
                        double techDouble1=atLast2double[i2][0];
                        double techDouble2=atLast2double[i2][1];
                        atLast2double[i2][0]=atLast2double[i2+1][0];
                        atLast2double[i2][1]=atLast2double[i2+1][1];
                        atLast2double[i2+1][0]=techDouble1;
                        atLast2double[i2+1][1]=techDouble2;
                        somethingElseToSort=true;
                    }
                }
            }
            
            // теперь можно сравнивать массивы
            for (int i2=0; i2<len; i2++){
                if (!(Arrays.equals(atLast1double[i2], atLast2double[i2]))) return RETURN_CODE_NUMBERS_NOT_EQUAL;
            }
        }
    }
    //если ни одна из проверок не сформировала возвращаемое значение неравенства строк, то строки равны!
    return RETURN_CODE_PAIRS_MATCH;
}

private int compareMaps(LinkedHashMap<String,String[]> map1, LinkedHashMap<String,String[]> map2){
    /*этот метод получает результаты работы метода
    LinkedHashMap<String,String[]> parseLineAsXML(String a) и возвращает код неравенства строк*/
    
    if (!(map1.size()==map2.size())) return RETURN_CODE_XML_DIFFERENT_NUMBER_OF_ELEMENTS;
    String[] sArray1=new String[map1.size()];
    String[] sArray2=new String[map2.size()];
    int i=0;
    for (Map.Entry<String,String[]> m:map1.entrySet()){
        sArray1[i]=m.getKey();
        i++;
    }
    i=0;
    for (Map.Entry<String,String[]> m:map2.entrySet()){
        sArray2[i]=m.getKey();
        i++;
    }    

    for (i=0; i<sArray1.length; i++){
        if (!(sArray1[i].equals(sArray2[i]))) return RETURN_CODE_XML_DIFFERENT_ELEMENTS;
        if (!(Arrays.equals(map1.get(sArray1[i]), map2.get(sArray1[i])))) return RETURN_CODE_XML_DIFFERENT_ATTRIBUTES;
    }
    return RETURN_CODE_XML_MATCH;
}

public ListOfMatches compare(File file1, File file2){
    
    String str1, str2;
    ArrayList<Integer> listOfMatches=new ArrayList();
    
    try{
        //начнём с открытия файлов
        BufferedReader br1=new BufferedReader(new FileReader(file1));
        BufferedReader br2=new BufferedReader(new FileReader(file2));
        
        str1=br1.readLine();
        
        //построчно проверяем равенство
        while (str1!=null) {
            str2=br2.readLine();
            if (str2==null) listOfMatches.add(RETURN_CODE_REACHED_EOF_IN_FILE_2);
            else {
                if (str1.trim().length()>0 && str1.trim().charAt(0)=='<'){
                    //возможно, это узел xml, парсим
                    try {
                        listOfMatches.add(compareMaps(parseLineAsXML(str1),parseLineAsXML(str2)));
                    } catch (SAXException e){
                        System.out.println(e);
                        listOfMatches.add(RETURN_CODE_XML_INVALID);
                    }
                } else{//это набор пар "ключ-зачение"
                    listOfMatches.add(comparePairs(str1,str2));
                }
            }
            str1=br1.readLine();
        }    
        if (!(br2.readLine()==null)){//EOF in file 1
            listOfMatches.add(RETURN_CODE_REACHED_EOF_IN_FILE_1);
        }
        
        //закроем reader`ы
        br1.close();
        br2.close();
        
    } catch (IOException e){
        System.out.println(e);
    }
    
    Integer[] intArray=(Integer[])listOfMatches.toArray(new Integer[0]);
    return new ListOfMatches(file1,file2,intArray);
}

    public static void main(String[] args) {
        //main используем для тестирования проекта
        
        FileComparator c=new FileComparator();
        ListOfMatches l=c.compare(new File("file1.txt"), new File("file2.txt"));
        l.printMismathes();
        
        System.out.println();
        System.out.println("Demonstration of 'ListOfMatches.getAll'");
        Integer[] arr=l.getAll();
        for (int i:arr){
            System.out.println(returnCodes.get(i));
        }
    }
}
