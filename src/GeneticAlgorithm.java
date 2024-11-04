import java.io.*;
import java.nio.file.*;
import java.util.*;

class City {
    String name;
    double x;
    double y;

    City(String name, double x, double y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }
}

class Route {
    List<Integer> path;
    double totalDistance;

    Route() {
        this.path = new ArrayList<>();
        this.totalDistance = 0;
    }

    public void calculateDistance(List<Double> cityXCoords, List<Double> cityYCoords) {
        this.totalDistance = 0;
        for (int i = 0; i < this.path.size() - 1; i++) {
            int fromCity = this.path.get(i);
            int toCity = this.path.get(i + 1);
            if (Double.isNaN(cityXCoords.get(fromCity)) || Double.isNaN(cityYCoords.get(fromCity))
                    || Double.isNaN(cityXCoords.get(toCity)) || Double.isNaN(cityYCoords.get(toCity))) {
                System.err.printf("Invalid coordinates for cities: from=%d, to=%d%n", fromCity, toCity);
                this.totalDistance = Double.NaN;
                return;
            }
            this.totalDistance += distance(cityXCoords.get(fromCity), cityYCoords.get(fromCity),
                    cityXCoords.get(toCity), cityYCoords.get(toCity));
        }
    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}

class PriorityQueueRoute {
    private final PriorityQueue<Route> data;

    PriorityQueueRoute() {
        this.data = new PriorityQueue<>(Comparator.comparingDouble(r -> r.totalDistance));
    }

    public void push(Route route) {
        this.data.add(route);
    }

    public Route pop() {
        return this.data.poll();
    }

    public Route top() {
        return this.data.peek();
    }

    public int size() {
        return this.data.size();
    }

    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    public void clear() {
        this.data.clear();
    }
}

public class GeneticAlgorithm {
    private static List<Double> cityXCoords = new ArrayList<>();
    private static List<Double> cityYCoords = new ArrayList<>();
    private static int cityCount;
    private static final PriorityQueueRoute populationQueue = new PriorityQueueRoute();
    private static final PriorityQueueRoute nextGenerationQueue = new PriorityQueueRoute();

    public static Route findBestRoute() {
        return populationQueue.top();
    }

    public static void displayRoute(Route route) {
        System.out.printf("Current best total distance: %.2f%n", route.totalDistance);
    }

    public static void mutate(Route route) {
        Random rand = new Random();
        int randomIndex1 = rand.nextInt(cityCount);
        int randomIndex2 = rand.nextInt(cityCount);
        Collections.swap(route.path, randomIndex1, randomIndex2);
    }

    public static void completeChildRoute(Route child, Route parent, int endIndex) {
        Set<Integer> visited = new HashSet<>(child.path);
        for (int i = endIndex + 1; i < cityCount; i++) {
            for (int nextCity : parent.path) {
                if (!visited.contains(nextCity)) {
                    child.path.add(nextCity);
                    visited.add(nextCity);
                    break;
                }
            }
        }
    }

    public static void crossOver(Route parent1, Route parent2) {
        Route child1 = new Route();
        Route child2 = new Route();

        Random rand = new Random();
        int endIndex = rand.nextInt(cityCount);

        for (int i = 0; i <= endIndex; i++) {
            child1.path.add(parent1.path.get(i));
            child2.path.add(parent2.path.get(i));
        }

        completeChildRoute(child1, parent2, endIndex);
        completeChildRoute(child2, parent1, endIndex);

        mutate(child1);
        mutate(child2);

        child1.calculateDistance(cityXCoords, cityYCoords);
        child2.calculateDistance(cityXCoords, cityYCoords);

        nextGenerationQueue.push(child1);
        nextGenerationQueue.push(child2);
    }

    public static void reproduce() {
        int initialSize = populationQueue.size();
        while (populationQueue.size() > initialSize / 2) {
            Route parent1 = populationQueue.pop();
            Route parent2 = populationQueue.pop();
            crossOver(parent1, parent2);

            nextGenerationQueue.push(parent1);
            nextGenerationQueue.push(parent2);
        }
        populationQueue.clear();
    }

    public static void initializeNextGeneration() {
        while (!nextGenerationQueue.isEmpty()) {
            populationQueue.push(nextGenerationQueue.pop());
        }
    }

    public static List<City> loadCitiesFromFile(String filename) throws IOException {
        List<City> cities = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filename + "_xy.csv"));
        List<String> names = Files.readAllLines(Paths.get(filename + "_name.csv"));

        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",");
            double x = Double.parseDouble(parts[0].trim());
            double y = Double.parseDouble(parts[1].trim());
            cities.add(new City(names.get(i).trim(), x, y));
        }
        return cities;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of cities or the filename to load cities: ");
        String input = scanner.nextLine();

        try {
            List<City> cities;
            if (input.matches("\\d+")) {
                cityCount = Integer.parseInt(input);
                cities = new ArrayList<>();
                Random rand = new Random();
                for (int i = 0; i < cityCount; i++) {
                    cities.add(new City("City" + (i + 1), rand.nextDouble() * 10, rand.nextDouble() * 10));
                }
            } else {
                cities = loadCitiesFromFile(input);
                cityCount = cities.size();
            }

            cityXCoords = new ArrayList<>();
            cityYCoords = new ArrayList<>();
            for (City city : cities) {
                cityXCoords.add(city.x);
                cityYCoords.add(city.y);
            }

            System.out.println("Number of cities: " + cities.size());
            int populationSize = 250;
            int generations = 2000;

            for (int i = 0; i < populationSize; i++) {
                Route route = new Route();
                for (int j = 0; j < cityCount; j++) {
                    route.path.add(j);
                }
                Collections.shuffle(route.path);
                route.calculateDistance(cityXCoords, cityYCoords);
                if (Double.isNaN(route.totalDistance)) {
                    System.err.println("NaN result found during initialization.");
                    continue;
                }
                populationQueue.push(route);
            }

            int generation = 0;
            Route bestRoute;

            while (generation <= generations) {
                if (Arrays.asList(10, 40, 80, 100, generations).contains(generation)) {
                    System.out.println((generation / 10) + ":");
                    bestRoute = findBestRoute();
                    displayRoute(bestRoute);
                }

                reproduce();
                initializeNextGeneration();
                generation++;
            }

            bestRoute = findBestRoute();
            System.out.println("\nBest path:");
            for (int index : bestRoute.path) {
                System.out.print(cities.get(index).name + " -> ");
            }
            System.out.println();
            System.out.printf("Total distance: %.2f%n", bestRoute.totalDistance);

        } catch (IOException e) {
            System.err.println("Error loading cities: " + e.getMessage());
        }

        scanner.close();
    }
}
