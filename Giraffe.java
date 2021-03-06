import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of a Giraffe.
 * giraffes age, move, eat acacia, and die.
 * 
 * @author David J. Barnes and Michael Kölling and Reibjok Othow
 * @version 27/02/2022
 */
public class Giraffe extends Consumer
{
    // Characteristics shared by all giraffes (class variables).
    
    // The age at which a Giraffe can start to breed.
    private static final int BREEDING_AGE = 8;
    // The age to which a Giraffe can live.
    private static final int MAX_AGE = 50;
    // The likelihood of a Giraffe breeding.
    private static final double BREEDING_PROBABILITY = 0.09;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 2;
    // The food value of a single acacia. In effect, this is the
    // number of steps a Giraffe can go before it has to eat again.
    private static final int ACACIA_FOOD_VALUE = 50;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    
    // Individual characteristics (instance fields).
    // The Giraffe's age.
    private int age;
    // The Giraffe's food level, which is increased by eating rabbits.
    private int foodLevel;

    /**
     * Create a Giraffe. A Giraffe can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the Giraffe will have random age and hunger level.
     * @param female whether or not the giraffe is female
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Giraffe(boolean randomAge,boolean female, Field field, Location location)
    {
        super(female, field, location);
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(ACACIA_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = ACACIA_FOOD_VALUE;
        }
    }
    
    /**
     * This is what the Giraffe does most of the time: it hunts for
     * acacia. In the process, it might breed, die of hunger,
     * or die of old age.
     * only female giraffes can breed
     * @param field The field currently occupied.
     * @param newgiraffes A list to return newly born giraffes.
     */
    public void act(List<Entity> newgiraffes)
    {
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            if(isFemale()){
                giveBirth(newgiraffes);
            }       
            // Move towards a source of food if found.
            Location newLocation = findFood();
            if(newLocation == null) { 
                // No food found - try to move to a free location.
                newLocation = getField().freeAdjacentLocation(getLocation());
            }
            // See if it was possible to move.
            if(newLocation != null) {
                setLocation(newLocation);
            }
            else {
                // Overcrowding.
                setDead();
            }
        }
    }

    /**
     * Increase the age. This could result in the Giraffe's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this Giraffe more hungry. This could result in the Giraffe's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Look for acacia adjacent to the current location.
     * Only the first live acacia is eaten.
     * @return Where food was found, or null if it wasn't.
     */
    protected Location findFood()
    {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object plant = field.getObjectAt(where);
            if(plant instanceof Acacia) {
                Acacia acacia = (Acacia) plant;
                if(acacia.isAlive()) { 
                    acacia.setDead();
                    foodLevel = ACACIA_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
    }
    
    /**
     * Check whether or not this Giraffe is to give birth at this step.
     * The giraffe breed when a male and female giraffe meet and mate
     * New births will be made into free adjacent locations.
     * @param newgiraffes A list to return newly born giraffes.
     */
    private void giveBirth(List<Entity> newgiraffes)
    {
        // New giraffes are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation(), 2);
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            boolean gender = rand.nextBoolean();
            Giraffe young = new Giraffe(false,gender,field, loc);
            newgiraffes.add(young);
        }
    }
        
    /**
     * This method checks if there is any male giraffe nearby so 
     * @return boolean there is a male nearby
     */
    private boolean canFindMaleGiraffe(int distance){
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation(),distance);
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Giraffe) {
                Giraffe giraffe = (Giraffe) animal;
                if(!giraffe.isFemale()) { 
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @return The number of births (may be zero).
     */
    private int breed()
    {
        int births = 0;
        if(canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
            births = rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        if (canFindMaleGiraffe(2)){
            return births;
        }
        else{
            return 0;
        }
    }

    /**
     * A Giraffe can breed if it has reached the breeding age.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
}
