import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class City {
    String name;
    double x;
    double y;

    public City(String name, double x, double y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public double distanceTo(City city) {
        double dx = this.x - city.x;
        double dy = this.y - city.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return this.name;
    }
}

class Chromosome {
    List<City> route;
    double fitness;

    public Chromosome(List<City> cities) {
        this.route = new ArrayList<>(cities);
        Collections.shuffle(this.route);
        calculateFitness();
    }

    public void calculateFitness() {
        double totalDistance = 0.0;
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
        StringBuilder path = new StringBuilder();
        for (City city : route) {
            path.append(city.name).append(" -> ");
        }
        path.append(route.get(0).name); // Complete the cycle
        return path.toString();
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

public class GeneticAlgorithm {
    private static final int POPULATION_SIZE = 100;
    private static final int NUM_GENERATIONS = 50000;
    private static final double MUTATION_RATE = 0.02;
    private static final Random random = new Random();

    public static void main(String[] args) {
        // Sample cities data (use your CSV parsing logic to read city data)
        List<City> cities = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        String fileName = scanner.nextLine();

        try (BufferedReader brCoords = new BufferedReader(new FileReader(fileName + "_xy.csv"));
             BufferedReader brNames = new BufferedReader(new FileReader(fileName + "_name.csv"))) {

            String line;
            while ((line = brCoords.readLine()) != null) {
                String name = brNames.readLine().trim();
                String[] coords = line.split(",");
                cities.add(new City(name, Double.parseDouble(coords[0]), Double.parseDouble(coords[1])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Population population = new Population(POPULATION_SIZE, cities);

        for (int generation = 1; generation <= NUM_GENERATIONS; generation++) {
            population = evolvePopulation(population);

            // Log progress at specific generations
            if (generation == 10 || generation == 100 || generation == 1000 || generation == 5000 || generation == 50000) {
                Chromosome bestChromosome = population.getFittest();
                System.out.println("Generation " + generation + " - Best Distance: " + bestChromosome.getDistance());
                System.out.println("Path: " + bestChromosome);
            }
        }

        // Print final result
        Chromosome bestChromosome = population.getFittest();
        System.out.println("Best route found after 50000 generations:");
        System.out.println("Distance: " + bestChromosome.getDistance());
        System.out.println("Path: " + bestChromosome);
    }

    private static Population evolvePopulation(Population population) {
        Population newPopulation = new Population(population.chromosomes.size(), population.chromosomes.get(0).route);

        // Elitism: Retain the best chromosome
        newPopulation.chromosomes.set(0, population.getFittest());

        // Crossover and mutation
        for (int i = 1; i < newPopulation.chromosomes.size(); i++) {
            Chromosome parent1 = tournamentSelection(population);
            Chromosome parent2 = tournamentSelection(population);
            Chromosome child = orderCrossover(parent1, parent2);
            mutate(child);
            child.calculateFitness();
            newPopulation.chromosomes.set(i, child);
        }

        return newPopulation;
    }

    private static Chromosome orderCrossover(Chromosome parent1, Chromosome parent2) {
        int size = parent1.route.size();
        List<City> childRoute = new ArrayList<>(Collections.nCopies(size, null));

        int start = random.nextInt(size);
        int end = random.nextInt(size);

        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        for (int i = start; i <= end; i++) {
            childRoute.set(i, parent1.route.get(i));
        }

        int currentIndex = (end + 1) % size;
        for (City city : parent2.route) {
            if (!childRoute.contains(city)) {
                childRoute.set(currentIndex, city);
                currentIndex = (currentIndex + 1) % size;
            }
        }

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
