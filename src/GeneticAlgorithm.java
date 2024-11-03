import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GeneticAlgorithm {
    private static final int POPULATION_SIZE = 100;
    private static final int NUM_GENERATIONS = 50000;
    private static final double MUTATION_RATE = 0.02;
    private static final Random random = new Random();
    private static final String COORDINATES_FILE_SUFFIX = "_xy.csv";
    private static final String NAMES_FILE_SUFFIX = "_name.csv";

    public static void main(String[] args) {
        List<City> cities = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        String fileName = scanner.nextLine();

        try (BufferedReader coordinatesReader = new BufferedReader(new FileReader(fileName + COORDINATES_FILE_SUFFIX));
             BufferedReader namesReader = new BufferedReader(new FileReader(fileName + NAMES_FILE_SUFFIX))) {

            String line;
            while ((line = coordinatesReader.readLine()) != null) {
                String name = namesReader.readLine().trim();
                String[] coordinates = line.split(",");
                cities.add(new City(name, Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Population population = new Population(POPULATION_SIZE, cities);

        for (int generation = 1; generation <= NUM_GENERATIONS; generation++) {
            population = evolvePopulation(population);

            if (generation == 10 || generation == 100 || generation == 1000 || generation == 5000 || generation == 50000) {
                Chromosome bestChromosome = population.getFittest();
                System.out.println("Generation " + generation + " - Best Distance: " + bestChromosome.getDistance());
                System.out.println("Path: " + bestChromosome);
            }
        }

        Chromosome bestChromosome = population.getFittest();
        System.out.println("Best route found after 50000 generations:");
        System.out.println("Distance: " + bestChromosome.getDistance());
        System.out.println("Path: " + bestChromosome);
    }

    private static Population evolvePopulation(Population currentPopulation) {
        Population evolvedPopulation = new Population(
                currentPopulation.chromosomes.size(),
                currentPopulation.chromosomes.get(0).route
        );

        // Elitism: Retain the best chromosome
        evolvedPopulation.chromosomes.set(0, currentPopulation.getFittest());

        // Crossover and mutation
        for (int i = 1; i < evolvedPopulation.chromosomes.size(); i++) {
            Chromosome parent1 = tournamentSelection(currentPopulation);
            Chromosome parent2 = tournamentSelection(currentPopulation);
            Chromosome child = orderCrossover(parent1, parent2);
            mutate(child);
            child.calculateFitness();
            evolvedPopulation.chromosomes.set(i, child);
        }

        return evolvedPopulation;
    }

    private static Chromosome orderCrossover(Chromosome firstParent, Chromosome secondParent) {
        int size = firstParent.route.size();
        List<City> childRoute = new ArrayList<>(Collections.nCopies(size, null));

        int start = random.nextInt(size);
        int end = random.nextInt(size);

        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        for (int i = start; i <= end; i++) {
            childRoute.set(i, firstParent.route.get(i));
        }

        AtomicInteger currentIndex = new AtomicInteger((end + 1) % size);
        secondParent.route.stream()
                .filter(Predicate.not(childRoute::contains))
                .forEach(city -> {
                    childRoute.set(currentIndex.get(), city);
                    currentIndex.set((currentIndex.get() + 1) % size);
                });

        return new Chromosome(childRoute);
    }

    private static void mutate(Chromosome chromosome) {
        for (int i = 0; i < chromosome.route.size(); i++) {
            if (Math.random() < MUTATION_RATE) {
                int j = random.nextInt(chromosome.route.size());
                Collections.swap(chromosome.route, i, j);
            }
        }
    }

    private static Chromosome tournamentSelection(Population population) {
        Chromosome firstCandidate = population.chromosomes.get(random.nextInt(population.chromosomes.size()));
        Chromosome secondCandidate = population.chromosomes.get(random.nextInt(population.chromosomes.size()));

        return firstCandidate.fitness > secondCandidate.fitness ? firstCandidate : secondCandidate;
    }
}

class Population {
    List<Chromosome> chromosomes;

    public Population(int populationSize, List<City> cities) {
        chromosomes = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            chromosomes.add(new Chromosome(cities));
        }
    }

    public Chromosome getFittest() {
        return Collections.max(chromosomes, Comparator.comparingDouble(c -> c.fitness));
    }
}

class Chromosome {
    private static final String ARROW_DELIMITER = " -> ";
    List<City> route;
    double fitness;

    public Chromosome(List<City> cities) {
        this.route = new ArrayList<>(cities);
        Collections.shuffle(this.route);
        calculateFitness();
    }

    public void calculateFitness() {
        double totalDistance = 0d;
        for (int i = 0; i < route.size() - 1; i++) {
            totalDistance += route.get(i).distanceTo(route.get(i + 1));
        }

        // Return to the starting city
        totalDistance += route.get(route.size() - 1).distanceTo(route.get(0));
        this.fitness = 1 / totalDistance;
    }

    public double getDistance() {
        return 1 / this.fitness;
    }

    @Override
    public String toString() {
        return route.stream()
                .map(City::toString)
                .collect(Collectors.joining(ARROW_DELIMITER)) + ARROW_DELIMITER + route.get(0).toString();
    }
}

class City {
    String name;
    double longitude;
    double latitude;

    public City(String name, double longitude, double latitude) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double distanceTo(City city) {
        double longitudeDistance = this.longitude - city.longitude;
        double latitudeDistance = this.latitude - city.latitude;
        return Math.sqrt(Math.pow(longitudeDistance, 2) + Math.pow(latitudeDistance, 2));
    }

    @Override
    public String toString() {
        return this.name;
    }
}
