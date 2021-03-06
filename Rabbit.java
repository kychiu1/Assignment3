import java.util.List;
import java.util.Random;
import java.util.Iterator;

/**
 * A simple model of a rabbit.
 * Rabbits age, move, eat grass, breed, and die.
 * 
 * @author David J. Barnes and Michael Kölling and Reibjok Othow and Kwan Yui Chiu
 * @version 27/02/2022
 */
public class Rabbit extends Consumer
{
    // Characteristics shared by all rabbits (class variables).

    // The age at which a rabbit can start to breed.
    private static final int BREEDING_AGE = 3;
    // The age to which a rabbit can live.
    private static final int MAX_AGE = 50;
    // The likelihood of a rabbit breeding.
    private static final double BREEDING_PROBABILITY = 0.16;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 3;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    private static final int GRASS_FOOD_VALUE = 50;
    // Individual characteristics (instance fields).
    
    // The rabbit's age.
    private int age;
    // The rabbit's hungerness.
    private int foodLevel;
    
    /**
     * Create a new rabbit. A rabbit may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the rabbit will have a random age.
     * @param female whether  or not the rabbit is female
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Rabbit(boolean randomAge,boolean female, Field field, Location location)
    {
        super(female, field, location);
        age = 0;
        this.foodLevel = GRASS_FOOD_VALUE;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
        }
    }
    
    /**
     * This is what the rabbit does most of the time - it runs and eats grass
     * around. Sometimes it will breed or die of old age.
     * @param newRabbits A list to return newly born rabbits.
     */
    public void act(List<Entity> newRabbits)
    {
        incrementAge();
        if(super.isAlive()) {
            //check if the rabbit isFemale and giveBIrth
            if(isFemale()){
                giveBirth(newRabbits);
            }       
            // Move to a source of food if found.
            Location newLocation = findFood();
            if(newLocation == null){
                newLocation = getField().freeAdjacentLocation(getLocation());
            }
            //if no food is found, move to a  new location
            if(newLocation != null) {
                setLocation(newLocation);
            }
            else if (newLocation == null){
                // Overcrowding.
                setDead();
            }
        }
    }

    /**
     * Increase the age.
     * This could result in the rabbit's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Check whether or not this rabbit is to give birth at this step.
     * The rabbits breed when a male and female rabbit meet and mate
     * New births will be made into free adjacent locations.
     * @param newRabbits A list to return newly born rabbits.
     */
    private void giveBirth(List<Entity> newRabbits)
    {
        // New rabbits are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation(), 2);
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            boolean gender = rand.nextBoolean();
            Rabbit young = new Rabbit(false,gender, field, loc);
            newRabbits.add(young);
        }
    }
    
    /**
     * This method checks if there is any male rabbits nearby 
     * @return whether there is a male nearby
     */
    private boolean canFindMaleRabbit(int distance){
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation(),distance);
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Rabbit) {
                Rabbit rabbit = (Rabbit) animal;
                if(!rabbit.isFemale()) { 
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
        if (canFindMaleRabbit(2)){
            return births;
        }
        else{
            return 0;
        }
    }

    /**
     * A rabbit can breed if it has reached the breeding age.
     * @return true if the rabbit can breed, false otherwise.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
    
    /**
     * Look for grass adjacent to the current location.
     * Only the first live grass is eaten.
     * @return Where food was found, or null if it wasn't.
     */
    protected Location findFood(){
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object plant = field.getObjectAt(where);
            if(plant instanceof Grass) {
                Grass grass = (Grass) plant;
                if(grass.isAlive()) { 
                    grass.setDead();
                    foodLevel = GRASS_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
    }
}
