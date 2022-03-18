package CSCI570;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Efficient {
    public static final int GAP_PENALTY = 30;
    public static String Filename;
    public static void main(String[] args) throws IOException {

        Filename = args[0];
        String s1 = getInputData().firstString;
        String s2 = getInputData().secondString;

        double beforeUsedMem = getMemoryInKB();
        double startTime = getTimeInMilliseconds();

        Input input = DivideandConquerAlignment(s1,s2);

        double afterUsedMem = getMemoryInKB();
        double endTime = getTimeInMilliseconds();
        double totalUsage = afterUsedMem - beforeUsedMem;
        double totalTime = endTime - startTime;
        System.out.println(beforeUsedMem);
        System.out.println(afterUsedMem);

        int score = calculateScore(input);
        CreateOutput(Integer.toString(score));
        CreateOutput(input.firstString);
        CreateOutput(input.secondString);
        CreateOutput(Double.toString(totalTime));
        CreateOutput(Double.toString(totalUsage));
    }

    private static double getTimeInMilliseconds() {
        return System.nanoTime()/10e6;
    }

    private static double getMemoryInKB() {
        double total = Runtime.getRuntime().totalMemory();
        return (total - Runtime.getRuntime().freeMemory())/10e3;
    }

    static class Input {
        String firstString;
        String secondString;

        public Input(String firstString, String secondString) {
            this.firstString = firstString;
            this.secondString = secondString;
        }
        public Input add(Input input){
            return new Input(this.firstString+input.firstString, this.secondString+input.secondString);
        }
    }

    public static Input getInputData() throws IOException {
        List<Integer> indexes1 = new ArrayList<>();
        List<Integer> indexes2 = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(Filename));
        String firstBaseString = reader.readLine();
        String temp = null;
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        while(pattern.matcher(temp = reader.readLine()).matches()){
            indexes1.add(Integer.parseInt(temp));
        }
        String secondBaseString = temp;
        while((temp = reader.readLine()) !=null) {
            indexes2.add(Integer.parseInt(temp));
        }
        String firstString = constructInputStrings(firstBaseString, indexes1);
        String secondString = constructInputStrings(secondBaseString, indexes2);
        return new Input(firstString,secondString);
    }

    public static String constructInputStrings(String basestring, List<Integer> indexes) {
        StringBuilder sb = new StringBuilder(basestring);
        for(int i:indexes){
            basestring = sb.insert(i+1,basestring).toString();
        }
        return basestring;
    }

    public static int getMismatchCost(char c1, char c2){
        Map<Character,Integer> map = new HashMap<>();
        map.put('A',0);
        map.put('C',1);
        map.put('G',2);
        map.put('T',3);
        int[][] cost = {{0,110,48,94},{110,0,118,48},{48,118,0,110},{94,48,110,0}};
        int i = map.get(c1);
        int j = map.get(c2);
        return cost[i][j];
    }

    public static Input getResultString(String s1, String s2){
        String res1 = "";
        String res2 = "";
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m+1][n+1];
        dp[0][0] = 0;
        for(int i = 0;i<m+1;i++){
            for(int j = 0;j<n+1;j++){
                if(i == 0){
                    dp[i][j] = j * GAP_PENALTY;
                }else if (j == 0){
                    dp[i][j] = i * GAP_PENALTY;
                }else{
                    int match = dp[i-1][j-1] + getMismatchCost(s1.charAt(i-1),s2.charAt(j-1));
                    int left = dp[i-1][j] + GAP_PENALTY;
                    int right = dp[i][j-1] + GAP_PENALTY;
                    dp[i][j] = Math.min(match,Math.min(left,right));
                }
            }
        }
        while((m !=0) && (n!= 0)){
            if(dp[m][n] == dp[m-1][n-1] + getMismatchCost(s1.charAt(m-1),s2.charAt(n-1))){
                res1 = s1.charAt(m-1) + res1;
                res2 = s2.charAt(n-1) + res2;
                m--;
                n--;
            }else if(dp[m][n] == dp[m-1][n] + GAP_PENALTY){
                res1 = s1.charAt(m-1) + res1;
                res2 = "_" + res2;
                m--;
            }else if(dp[m][n] == dp[m][n-1] + GAP_PENALTY){
                res1 = "_" + res1;
                res2 = s2.charAt(n-1) + res2;
                n--;
            }
        }
        while (m > 0) {
            res1 = s1.charAt(m-1) + res1;
            res2 = "_" + res2;
            m--;
        }
        while (n > 0) {
            res1 = "_" + res1;
            res2 = s2.charAt(n-1) + res2;
            n--;
        }
        return new Input(res1,res2);
    }

    public static int[][] SpaceEfficientAlignment(String a,String b){
        int m = a.length();
        int n = b.length();
        int[][] dp = new int[m+1][2];
        for(int i = 0;i < m+1;i++){
            dp[i][0] = i * GAP_PENALTY;
        }
        for(int j = 1;j<n+1;j++){
            dp[0][1] = j * GAP_PENALTY;
            for(int i = 1;i<m+1;i++){
                int min = Math.min(dp[i][0] + GAP_PENALTY, dp[i - 1][1] + GAP_PENALTY);
                int match = dp[i-1][0] + getMismatchCost(a.charAt(i-1),b.charAt(j-1));
                dp[i][1] = Math.min(match,min);
            }
            for(int i = 0;i<m+1;i++){
                dp[i][0] = dp[i][1];
            }
        }
        return dp;
    }

    public static Input DivideandConquerAlignment(String a,String b){
        int m = a.length();
        int n = b.length();
        int midn = n/2;
        if(m<=2 || n<=2){
            return getResultString(a,b);
        }
        int[][] forward = SpaceEfficientAlignment(a,b.substring(0,midn));
        String arev = new StringBuilder(a).reverse().toString();
        String brev = new StringBuilder(b.substring(midn,n)).reverse().toString();
        int[][] backward = SpaceEfficientAlignment(arev,brev);
        int q = getMin(forward,backward);

        Input res1 = DivideandConquerAlignment(a.substring(0,q),b.substring(0,midn));
        Input res2 = DivideandConquerAlignment(a.substring(q,m),b.substring(midn,n));
        Input res = res1.add(res2);
        return res;
    }

    private static int getMin(int[][] a,int[][] b){
        int m = a.length;
        int min = Integer.MAX_VALUE;
        int index = 0;
        for(int i = 0;i<m;i++){
            if(a[i][0]+b[m-i-1][0] < min){
                min = a[i][0] + b[m-i-1][0];
                index = i;
            }
        }
        return index;
    }

    public static int calculateScore(Input input) {
        String a = input.firstString;
        String b = input.secondString;
        int score = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) == '_' || b.charAt(i) == '_') {
                score += GAP_PENALTY;
            } else {
                score += getMismatchCost(a.charAt(i), b.charAt(i));
            }
        }
        return score;
    }

    public static void CreateOutput(String s1){
        try {
            File file = new File("output.txt");
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsolutePath(),true);
            System.out.println(file.getAbsolutePath());
            BufferedWriter bw = new BufferedWriter(fw);
            if(s1 != null){
                bw.write(s1+"\r\n");
            }
            bw.close();
            fw.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

}
