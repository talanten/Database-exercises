import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.lang.model.util.ElementScanner6;


public class JoinOrderingExercise {

    /*
     *
     * To execute the program you then have to use:
     *
     *     java JoinOrderingExercise.java data.txt
     *
     * For the exercise write the code of the methods computeGreedy1-3, computeBestPlan, and computeWorstPlan.
     *
     * You can either create the data.txt file yourself by filling it (line by line) with the following data:
     * <Relation> <Cardinality>
     * ...
     * <Relation1> <Relation2> <selectivity>
     * ...
     * e.g.:
     * R1 100
     * R2 200
     * R1 R2 0.5
     * 
     * or you can download the data.txt for this exercise from OLAT
     */
    public static void main(String... args) throws IOException {
        System.out.println("Join order exercise:");
        System.out.println("============================================");

        if(args.length == 0) {
            System.out.println("Missing file argument!");
            System.exit(1);
        }

        File input = new File(args[0]);
        //Read config file
        DummyFileDirectory relations = new DummyFileDirectory(input);
        System.out.println("============================================");

        boolean valid = true;

        // Greedy 1
        JoinTree plan = computeGreedy1(relations);
        if (plan == null){
            System.out.println("Greedy 1: No plan found!");
            valid = false;
        }else{
            System.out.println("Greedy 1: \t" + plan.toString() + "\tCost: " + plan.cost(relations));
        }
        
        // Greedy 2
        plan = computeGreedy2(relations);
        if (plan == null){
            System.out.println("Greedy 2: No plan found!");
            valid = false;
        }else{
            System.out.println("Greedy 2: \t" + plan.toString() + "\tCost: " + plan.cost(relations));
        }

        // Greedy 3
        plan = computeGreedy3(relations);
        if (plan == null){
            System.out.println("Greedy 3: No plan found!");
            valid = false;
        }else{
            System.out.println("Greedy 3: \t" + plan.toString() + "\tCost: " + plan.cost(relations));
        }

        // Best
        plan = computeBestPlan(relations);
        if (plan == null){
            System.out.println("Best: No plan found!");
            valid = false;
        }else{
            System.out.println("Best: \t" + plan.toString() + "\tCost: " + plan.cost(relations));
        }

        // Worst
        plan = computeWorstPlan(relations);
        if (plan == null){
            System.out.println("Worst: No plan found!");
            valid = false;
        }else{
            System.out.println("Worst: \t" + plan.toString() + "\tCost: " + plan.cost(relations));
        }

        if (!valid){
            System.exit(2);
        }
    }



    /*   
    * The main components of this boilerplate code are the classes 
    *
    *   IRelationDirectory :        Interface representing a connection to a (mock) database, which provides metadata for relations.
    *       DummyFileDirectory :    This class uses a file to read relation information
    *   The function IRelationDirectory.getRelations() returns a list of all relations with their cardinalities.
    *   IRelationDirectory.getSelectivity(String a,String a,String pred) returns the selectivity between the relations a and b. (pred is unused and can be ignores)
    *
    *   JoinTree :      Interface representing a join tree with cost/cardinality calculation functions
    *       Join :      Represents a node in the join tree
    *       Relation :  Represents a leaf in the join tree
    *   You can create/modify a JoinTree by using the constructors:
    *       Relation(String name) : Creates a leaf node with the given relation name
    *       Join(JoinTree left, JoinTree right): Creates a node combining two JoinTrees
    *   The functions JoinTree.cost(IRelationDirectory) and JoinTree.cardinality(IRelationDirectory) 
    *   Can be used to calculate the cost and cardinality of a given JoinTree
    *
    * 
    *   There are additional useful functions, which should be understandable by reading the comments.
    */
    private static int findArgMin(List<Pair<String, Integer>> relations) {

        int argMinIdx = 0;
        for (int i=1; i < relations.size(); i++) {
            if (relations.get(i).getValue() < relations.get(argMinIdx).getValue())
                argMinIdx = i;
        }

        return argMinIdx;
    }

    private static int findArgMin(IRelationDirectory metadata, List<Pair<String, Integer>> relations, JoinTree jt) {

        int argMinIdx = 0;
        int minCost = Integer.MAX_VALUE;

        if (jt == null) {
            return findArgMin(relations);
        } else {
            JoinTree relation = new Relation(relations.get(argMinIdx).getKey());
            JoinTree joinTree = new Join(jt, relation);
            minCost = joinTree.cost(metadata);
        }

        for (int i=1; i < relations.size(); i++) {

            JoinTree tempTree = new Join(jt, new Relation(relations.get(i).getKey()));
            int currentCost = tempTree.cost(metadata);

            if (currentCost < minCost) {
                argMinIdx = i;
                minCost = currentCost;
            }
        }

        return argMinIdx;
    }

    static JoinTree computeGreedy1(IRelationDirectory directory) {

        JoinTree jtree = null;
        Relation leftmostRelation = null;
        List<Pair<String, Integer>> relations = directory.getRelations();
        while (relations.size() > 0) {
            int m = findArgMin(relations);
            if (jtree != null)
                jtree = new Join(jtree, new Relation(relations.get(m).getKey()));
            else if (jtree == null && leftmostRelation != null)
                jtree = new Join(leftmostRelation, new Relation(relations.get(m).getKey()));
            else if (jtree == null && leftmostRelation == null)
                    leftmostRelation = new Relation(relations.get(m).getKey());
            else System.out.println("!!!Something wrong happened in Greedy-1!!!");

            relations.remove(m);
        }
       
        return jtree;
    }

    static JoinTree computeGreedy2(IRelationDirectory directory) {
        JoinTree jtree = null;
        Relation leftmostRelation = null;
        List<Pair<String, Integer>> relations = directory.getRelations();
        while (relations.size() > 0) {
            int m = -1;

            if (jtree == null && leftmostRelation != null) 
                m = findArgMin(directory, relations, leftmostRelation);
            else m = findArgMin(directory, relations, jtree);

            if (jtree != null)
                jtree = new Join(jtree, new Relation(relations.get(m).getKey()));
            else if (jtree == null && leftmostRelation != null)
                jtree = new Join(leftmostRelation, new Relation(relations.get(m).getKey()));
            else if (jtree == null && leftmostRelation == null)
                    leftmostRelation = new Relation(relations.get(m).getKey());
            else System.out.println("!!!Something wrong happened in Greedy-1!!!");

            relations.remove(m);
        }
        return jtree;
    }

    static JoinTree computeGreedy3(IRelationDirectory directory) {
        JoinTree optimalJTree = null;
        List<JoinTree> jtreesWdiff1stRelation = new ArrayList<JoinTree>();

        for (int i = 0; i < directory.getRelations().size(); i++) {
            JoinTree jtree = null;
            Relation leftmostRelation = null;
            List<Pair<String, Integer>> relations = directory.getRelations();
            leftmostRelation = new Relation(relations.get(i).getKey());
            relations.remove(i);
        
            while (relations.size() > 0) {
                int m = -1;

                if (jtree == null && leftmostRelation != null) 
                    m = findArgMin(directory, relations, leftmostRelation);
                else m = findArgMin(directory, relations, jtree);

                if (jtree != null)
                    jtree = new Join(jtree, new Relation(relations.get(m).getKey()));
                else if (jtree == null && leftmostRelation != null)
                    jtree = new Join(leftmostRelation, new Relation(relations.get(m).getKey()));
                else System.out.println("!!!Something wrong happened in Greedy-1!!!");

                relations.remove(m);
            }

            jtreesWdiff1stRelation.add(jtree);
        }

        int minCost = Integer.MAX_VALUE;
        for (JoinTree joinTree : jtreesWdiff1stRelation) {
            if (joinTree.cost(directory) < minCost) {
                minCost = joinTree.cost(directory);
                optimalJTree = joinTree;
            }
        }

        return optimalJTree;
    }

    static JoinTree computeBestPlan(IRelationDirectory directory) {

        List<JoinTree> allPlans = getAllPlans(directory);

        JoinTree bestPlan = allPlans.get(0);
        for (int i=1; i < allPlans.size(); i++) {
            if (allPlans.get(i).cost(directory) < bestPlan.cost(directory))
                bestPlan = allPlans.get(i);
        }

        return bestPlan;
    }

    static JoinTree computeWorstPlan(IRelationDirectory directory) {

        List<JoinTree> allPlans = getAllPlans(directory);

        JoinTree worstPlan = allPlans.get(0);
        for (int i=1; i < allPlans.size(); i++) {
            if (allPlans.get(i).cost(directory) > worstPlan.cost(directory))
                worstPlan = allPlans.get(i);
        }

        return worstPlan;
    }

    private static List<JoinTree> getAllPlans(IRelationDirectory directory){
        List<JoinTree> allPlans = getAllPlans(directory, directory.getRelations().size());
        List<JoinTree> ret = new ArrayList<>();
        for (JoinTree plan : allPlans) {
            if (plan.getRelations().size() == directory.getRelations().size()){
                ret.add(plan);
            }
        }
        return ret;
    }
    private static List<JoinTree> getAllPlans(IRelationDirectory directory, int i) {

        if (i == 1){
            List<Pair<String, Integer>> relations = directory.getRelations();
            List<JoinTree> ret = new ArrayList<>();
            for (Pair<String, Integer> relation : relations) {
                ret.add(new Relation(relation.getKey()));
            }
            return ret;
        }
        List<JoinTree> plans = getAllPlans(directory, i - 1);
        List<JoinTree> combinedPlans = new ArrayList<>();
        for (JoinTree left : plans) {
            for (JoinTree right : plans) {
                if(!left.shareRelations(right)){
                    Join join = new Join(left, right);
                    if(!join.isCrossProduct(directory)){
                        combinedPlans.add(join);
                    }
                }
            }
        }
        plans.addAll(combinedPlans);
        return plans;
    }


}

////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////



/**
 * An interface representing a query plan
 */
interface JoinTree {
    /**
     * Returns the C_out costs of the query plan
     * @param directory A relation directory for selectivity and cardinality lookup.
     * @return The C_out costs of the query plan
     */
    int cost(IRelationDirectory directory);

    /**
     * Returns the cardinality of the query plan
     * @param directory A relation directory for selectivity and cardinality lookup.
     * @return The cardinality of the query plan
     */
    int cardinality(IRelationDirectory directory);

    /**
     * Returns a list of all contained relations
     * @return list of all contained relations
     */
    List<String> getRelations();

    /**
     * Checks if a given relation is contained within the query plan
     * @param relation The name of the relation
     * @return True if the relation is contained within the plan, false if not
     */
    boolean contains(String relation);

    /**
     * Check whether both plans share any relations.
     * @param plan The plan to check against
     * @return True if both plans share relations, false if not
     */
    boolean shareRelations(JoinTree plan);

    /**
     * Checks if the given plan contains any crossproducts
     * @param directory The relation directory to compute the crossproducts
     * @return True if the plan contains any crossproducts, false if not
     */
    boolean hasCrossProduct(IRelationDirectory directory);

}

/**
 * The Join class represents the inner nodes of the join tree
 */
class Join implements JoinTree {
    private JoinTree left;
    private JoinTree right;

    public Join(JoinTree left, JoinTree right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int cost(IRelationDirectory directory) {
        return cardinality(directory)+left.cost(directory)+right.cost(directory);
    }

    @Override
    public int cardinality(IRelationDirectory directory) {
        double factor = getSelectivityProduct(directory);

        return Math.toIntExact(Math.round(factor * left.cardinality(directory) * right.cardinality(directory)));
    }


    @Override
    public String toString() {
          return "(" + left.toString() + " \u2A1D " + right.toString() + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public List<String> getRelations() {
        List<String> relations = left.getRelations();
        relations.addAll(right.getRelations());
        return relations;
    }

    @Override
    public boolean contains(String relation) {
        return left.contains(relation) || right.contains(relation);
    }

    @Override
    public boolean shareRelations(JoinTree plan) {
        return left.shareRelations(plan) || right.shareRelations(plan);
    }

    @Override
    public boolean hasCrossProduct(IRelationDirectory directory){
        if (isCrossProduct(directory)){
            return true;
        }
        return left.hasCrossProduct(directory) || right.hasCrossProduct(directory);
    }

    /**
     * This function checks whether a cross product has to be computed to perform this join
     * @param directory A directory for relational metadata lookup
     * @return True if a cross product is computed, false if not
     */
    public boolean isCrossProduct(IRelationDirectory directory){
        double factor = getSelectivityProduct(directory);
        return factor == 1;
    }


    /**
     * Returns the selectivity product of this join tree
     * @param directory A directory for relational metadata lookup
     * @return A factor in [0,1]
     */
    private double getSelectivityProduct(IRelationDirectory directory) {
        double factor = 1;
        List<String> leftRelations = left.getRelations();
        List<String> rightRelations = right.getRelations();
        for (String l : leftRelations) {
            for (String r : rightRelations) {
                factor *= directory.getSelectivity(l,r,"");
            }
        }
        return factor;
    }
}

/**
 * This class represents the leafs of the join tree (single relations)
 */
class Relation implements JoinTree {
    private String relation;

    public Relation(String relation) {
        this.relation = relation;
     }

    @Override
    public int cost(IRelationDirectory directory) {
        return 0;
    }

    @Override
    public int cardinality(IRelationDirectory directory) {
        return directory.getSize(relation);
    }

    @Override
    public String toString() {
        return relation;
    }

    @Override
    public int hashCode() {
        return relation.hashCode();
    }

    @Override
    public List<String> getRelations() {
        ArrayList<String> list = new ArrayList<>();
        list.add(relation);
        return list;
    }

    @Override
    public boolean contains(String relation) {
        return relation.equals(this.relation);
    }

    @Override
    public boolean shareRelations(JoinTree plan) {
        return plan.getRelations().contains(relation);
    }

    @Override
    public boolean hasCrossProduct(IRelationDirectory directory) {
        return false;
    }
}

/**
 * This interface is used to retrieve metadata (size/selectivity/...) of relations.
 */
interface IRelationDirectory {

    /**
     * Retrieves all relations with their sizes
     * @return List of pairs containing relations and their sizes
     */
    List<Pair<String,Integer>> getRelations();

    /**
     * Returns the size of the given relation
     * @param relation name of the relation
     * @return Size of the relation
     */
    int getSize(String relation) throws NullPointerException;

    /**
     * Returns the selectivity of the given join
     * @param relationA First relation to check
     * @param relationB Second relation to check
     * @param predicate The join predicate
     * @return The selectivity of the join
     */
    double getSelectivity(String relationA, String relationB, String predicate) throws NullPointerException;

    /**
     * Returns the size of the given join.
     * @param relationA First relation to check
     * @param relationB Second relation to check
     * @param predicate The join predicate
     * @return Size of the join
     */
    int getJoinSize(String relationA, String relationB, String predicate) throws NullPointerException;
}

/**
 * The DummyFileDirectory uses a local file to read mock-metadata.
 * The file has to the following syntax:
 *
 *      Relation1   [Size]
 *              ...
 *      RelationN   [Size]
 *      Relation_i  Relation_j  [Selectivity]
 *              ...
 *
 */
class DummyFileDirectory implements IRelationDirectory {
    private HashMap<String,Integer> relations;
    private HashMap<Pair<String,String>,Double> selectivities;


    public DummyFileDirectory(File file) throws IOException{
        if (!file.exists()) throw new IOException("File does not exist");
        System.out.println("Reading file \""+file.getPath()+"\" ...");
        relations = new HashMap<>();
        selectivities = new HashMap<>();

        // Create a File scanner
        Scanner sc = new Scanner(file).useDelimiter("\n");

        int i = 0;
        while (sc.hasNextLine()){ //Read each line
            String line = sc.nextLine();
            if(line.isBlank()) continue; //Ignore empty lines
            String[] split = line.split("\\s+");
            if (split.length == 2){ //If the line consists of two parts: Read relations and size
                try{
                    relations.put(split[0],Integer.parseInt(split[1]));
                    System.out.println("Added relation \""+split[0]+"\" with size " + Integer.parseInt(split[1]));
                }catch (NumberFormatException e){
                    throw new IOException("Expected number but got string in line " + i);
                }
            }else if (split.length == 3){//If the line consists of three parts: Read relations and selectivity
                try{
                    selectivities.put(new Pair<>(split[0],split[1]),Double.parseDouble(split[2]));
                    selectivities.put(new Pair<>(split[1],split[0]),Double.parseDouble(split[2]));
                    System.out.println("Added selectivity of "+Double.parseDouble(split[2])+" between \"" + split[0] +
                            "\" and \"" + split[1] + "\"");
                }catch (NumberFormatException e){
                    throw new IOException("Expected number but got string in line " + i);
                }
            }else throw new IOException("Wrong syntax in line " + i);

            i++;
        }

    }

    @Override
    public int getSize(String relation) {
        if(!relations.containsKey(relation)) throw new NullPointerException("Relation \"" + relation +"\" not found.");
        return relations.get(relation);
    }

    @Override
    public double getSelectivity(String relationA, String relationB, String predicate) {
        Pair<String, String> pair = new Pair<>(relationA, relationB);
        if(!selectivities.containsKey(pair)) return 1; //Cross product
        return selectivities.get(pair);
    }

    @Override
    public int getJoinSize(String relationA, String relationB, String predicate) {
        return Math.toIntExact(Math.round(getSize(relationA) * getSize(relationB) * getSelectivity(relationA, relationB, "")));
    }

    @Override
    public List<Pair<String, Integer>> getRelations() {
        ArrayList<Pair<String,Integer>> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : relations.entrySet()) {
            list.add(new Pair<>(entry.getKey(),entry.getValue()));
        }
        return list;
    }
}


class Pair<K,V> extends AbstractMap.SimpleEntry<K,V> {
    public Pair(K k, V v) {
        super(k,v);
    }
}