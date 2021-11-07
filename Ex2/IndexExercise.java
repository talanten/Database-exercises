import java.util.*;


/**
 * Exxecute the code with:
 * 
 *     java IndexExercise.java
 *
 * For the exercise complete the implementation of DenseIndex.
 * Overwrite the methods getEqualStringTuples and getGreaterEqualsStringTuples or note why this is not possible for your given index.
 * You may use any auxillary data structures you want in your indices (HashMaps, Trees, Lists, ...) and sort your underlying data in a different manner.
 *
 */
public class IndexExercise {
    public static void main(String... args) {
        System.out.println("Index exercise");
        System.out.println("================================");
        System.out.println("Generating data...");
        DataGenerator gen = new DataGenerator(500000);
        String queryString = gen.getRandomString();
        System.out.println("================================");

        //Default execution
        System.out.println("Testing default");
        long creationStartTime = System.currentTimeMillis();
        Index index = new Index(gen.tuples);
        long creationEndTime = System.currentTimeMillis();
        long equalExecutionStartTime = System.currentTimeMillis();
        List<Tuple> equalResult = index.getEqualStringTuples(queryString);
        long equalExecutionEndTime = System.currentTimeMillis();
        long greaterExecutionStartTime = System.currentTimeMillis();
        List<Tuple> greaterResult =index.getGreaterEqualsStringTuples(queryString);
        long greaterExecutionEndTime = System.currentTimeMillis();
        Collections.sort(equalResult);
        Collections.sort(greaterResult);
        System.out.println("Creating Index: " + (creationEndTime - creationStartTime) + "ms.");
        System.out.println("Executing Equality: " + (equalExecutionEndTime - equalExecutionStartTime) + "ms.");
        System.out.println("Executing Greater: " + (greaterExecutionEndTime - greaterExecutionStartTime) + "ms.");
        System.out.println("================================");

        //Dense index
        System.out.println("Testing dense");
        boolean ret = dense_execution(gen.tuples,queryString,equalResult,greaterResult);
        System.out.println("================================");

        if (!ret)
        {
            System.exit(1);
        }
        System.exit(0);
    }

    private static boolean dense_execution(ArrayList<Tuple> tuples, String string, List<Tuple> expectedEqualResult, List<Tuple> expectedGreaterResult){
        long creationStartTime = System.currentTimeMillis();
        Index index = new DenseIndex(tuples);
        long creationEndTime = System.currentTimeMillis();
        System.out.println("Creating Index: " + (creationEndTime - creationStartTime) + "ms.");

        return testResults(string, index,expectedEqualResult,expectedGreaterResult);
    }


    private static boolean testResults(String string, Index index, List<Tuple> expectedEqualResult, List<Tuple> expectedGreaterResult) {
        long equalExecutionStartTime = System.currentTimeMillis();
        List<Tuple> equalResult = index.getEqualStringTuples(string);
        long equalExecutionEndTime = System.currentTimeMillis();

        long greaterExecutionStartTime = System.currentTimeMillis();
        List<Tuple> greaterResult = index.getGreaterEqualsStringTuples(string);
        long greaterExecutionEndTime = System.currentTimeMillis();

        System.out.println("Executing Equality: " + (equalExecutionEndTime - equalExecutionStartTime) + "ms.");
        System.out.println("Executing Greater: " + (greaterExecutionEndTime - greaterExecutionStartTime) + "ms.");

        boolean correct = true;
        Collections.sort(equalResult);
        if(equalResult.equals(expectedEqualResult)) {
            System.out.println("Equality result correct.");
        } else {
            System.out.println("Equality result incorrect!");
            correct = false;
        }

        Collections.sort(greaterResult);
        if(greaterResult.equals(expectedGreaterResult)) {
            System.out.println("Greater result correct.");
        } else {
            System.out.println("Greater result incorrect!");
            correct = false;
        }
        return correct;
    }

}

/**
 * This class simulates a row in the database.
 * It consists of a unique ID attribute and a string value.
 */
class Tuple implements Comparable<Tuple> {
    final String string;
    private final int id;

    Tuple(int id, String string) {
        this.id = id;
        this.string = string;
    }

    @Override
    public int compareTo(Tuple o) {
        return Integer.compare(this.id, o.id);
    }

    @Override
    public int hashCode() {
        return this.id ^ this.string.hashCode();
    }
}

/**
 * This class simulates an index within the database.
 * It has the full list of tuples as member and can perform two actions:
 * getEqualStringTuples and getGreaterEqualsStringTuples
 */
class Index {
    ArrayList<Tuple> tuples;

    /**
     * Initializes the Index with the underlying tuples
     * @param tuples The tuples on which the index is based
     */
    Index(ArrayList<Tuple> tuples) {
        this.tuples = tuples;
    }

    /**
     * @param string The query string
     * @return a list of tuples for which the string is equal to the query string.
     */
    public List<Tuple> getEqualStringTuples(String string) {
        ArrayList<Tuple> ret = new ArrayList<>();
        for (Tuple tuple : tuples) {
            if(tuple.string.equals(string)){
                ret.add(tuple);
            }
        }
        return ret;
    }

    /**
     * @param string The query string
     * @return a list of tuples for which the string is equal or greater to the query string.
     */
    public List<Tuple> getGreaterEqualsStringTuples(String string) {
        ArrayList<Tuple> ret = new ArrayList<>();
        for (Tuple tuple : tuples) {
            if(tuple.string.compareTo(string) >= 0){
                ret.add(tuple);
            }
        }
        return ret;
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////
//   _____ _                              _______ _     _     
//  / ____| |                            |__   __| |   (_)    
// | |    | |__   __ _ _ __   __ _  ___     | |  | |__  _ ___ 
// | |    | '_ \ / _` | '_ \ / _` |/ _ \    | |  | '_ \| / __|
// | |____| | | | (_| | | | | (_| |  __/    | |  | | | | \__ \
//  \_____|_| |_|\__,_|_| |_|\__, |\___|    |_|  |_| |_|_|___/
//                            __/ |                           
//                           |___/                            
//////////////////////////////////////////////////////////////////////////////////////////////////////
class DenseIndex extends Index {

    Map<String, List<Integer>> denseIndex;

    DenseIndex(ArrayList<Tuple> tuples) {
        super(tuples);
        denseIndex = new HashMap<>();
        for (Tuple tuple : tuples) {
            denseIndex.computeIfAbsent(tuple.string, k -> new ArrayList<>()).add(tuple.hashCode());
        }
    }

    @Override
    /**
     * @param string The query string
     * @return a list of tuples for which the string is equal to the query string.
     */
    public List<Tuple> getEqualStringTuples(String string) {
        ArrayList<Tuple> ret = new ArrayList<>();
        List<Integer> pointers = this.denseIndex.get(string);
        for (int p : pointers) {
            Tuple tuple = tuples.stream()
                        .filter(t -> t.hashCode() == p)
                        .findFirst()
                        .orElse(null);

            if (tuple == null)
                System.out.println("!!!NULL object found for string: " + string);
            else
                ret.add(tuple);
        }
        
        return ret;
    }

    @Override
    /**
     * @param string The query string
     * @return a list of tuples for which the string is equal or greater to the query string.
     */
    public List<Tuple> getGreaterEqualsStringTuples(String string) {
        ArrayList<Tuple> ret = new ArrayList<>();
        for (Tuple tuple : tuples) {
            if(tuple.string.compareTo(string) >= 0){
                ret.add(tuple);
            }
        }
        return ret;
    }

}
//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * This class generates the Tuples
 */
class DataGenerator {
    private ArrayList<String> strings;
    ArrayList<Tuple> tuples;

    DataGenerator(int numLines){
        strings = new ArrayList<>();
        tuples = new ArrayList<>();
        // Number of distinct Strings to generate
        int numStrings = numLines/10;
        for (int i = 0; i < numStrings; i++) {
            // It is no problem if a duplicate is already in the list,
            // this just means, that this string will be chosen more often
            strings.add(getAlphaNumericString(10));
        }

        // Add tuples with unique ids and one of the generated strings
        Random random = new Random();
        for (int i = 0; i < numLines; i++) {
            tuples.add(new Tuple(i,getRandomString()));
        }
    }

    /**
     * @return One of the random pre-generated strings
     */
    String getRandomString() {
        Random random = new Random();
        return strings.get(random.ints(0,strings.size()).findFirst().getAsInt());
    }

    /**
     * @return a random collection of strings, with 1/10 the size of the whole collection
     */
    Set<String> getSparseAttributes() {
        Set<String> set = new HashSet<>(strings);
        while(set.size() < strings.size()/10){
            set.add(getRandomString());
        }

        return set;
    }

    private static String getAlphaNumericString(int n)
    {
        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

}
