import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class GeneticAlgorithm {

    public static final List<Double> CITY_LONGITUDES = new ArrayList<>();
    public static final List<Double> CITY_LATITUDES = new ArrayList<>();
    private static int CITY_COUNT;
    private static final int POPULATION_SIZE = 250;
    private static final int GENERATIONS = 2000;
    private static final PriorityQueue<Route> populationQueue = new PriorityQueue<>(
            Comparator.comparingDouble(route -> route.totalDistance)
    );
    private static final PriorityQueue<Route> nextGenerationQueue = new PriorityQueue<>(
            Comparator.comparingDouble(route -> route.totalDistance)
    );
    private static final List<Integer> CHECKPOINTS = List.of(5, 50, 100, 1000, GENERATIONS);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the file name to load cities: ");
        String input = scanner.nextLine();

        try {
            List<City> cities = loadCitiesFromFile(input);
            CITY_COUNT = cities.size();

            for (City city : cities) {
                CITY_LONGITUDES.add(city.longitude);
                CITY_LATITUDES.add(city.latitude);
            }

            System.out.println("Number of cities: " + cities.size());
            Visualizer.display(cities, Collections.emptyList());

            for (int i = 0; i < POPULATION_SIZE; i++) {
                Route route = new Route();
                for (int j = 0; j < CITY_COUNT; j++) {
                    route.path.add(j);
                }
                Collections.shuffle(route.path);
                route.calculateDistance(CITY_LONGITUDES, CITY_LATITUDES);
                populationQueue.add(route);
            }

            Route bestRoute;
            int generation = 0;
            while (generation <= GENERATIONS) {
                if (CHECKPOINTS.contains(generation)) {
                    System.out.println();
                    bestRoute = populationQueue.peek();

                    assert bestRoute != null;
                    System.out.printf("Current best total distance: %s".formatted(bestRoute.totalDistance));
                }
                reproduce();

                while (!nextGenerationQueue.isEmpty()) {
                    populationQueue.add(nextGenerationQueue.poll());
                }
                generation++;
            }

            bestRoute = populationQueue.peek();
            assert bestRoute != null;
            System.out.printf("\nTotal distance: %s".formatted(bestRoute.totalDistance));

            System.out.println("\nVisualizing the shortest path...");
            Visualizer.display(cities, bestRoute.path);

        } catch (IOException e) {
            System.err.println("Error loading cities: " + e.getMessage());
        }

        scanner.close();
    }

    public static void mutate(Route route) {
        Random random = new Random();
        int firstRandomIndex = random.nextInt(CITY_COUNT);
        int secondRandomIndex = random.nextInt(CITY_COUNT);
        Collections.swap(route.path, firstRandomIndex, secondRandomIndex);
    }

    public static void completeChildRoute(Route child, Route parent, int endIndex) {
        Set<Integer> visited = new HashSet<>(child.path);
        for (int i = endIndex + 1; i < CITY_COUNT; i++) {
            for (int nextCity : parent.path) {
                if (!visited.contains(nextCity)) {
                    child.path.add(nextCity);
                    visited.add(nextCity);
                    break;
                }
            }
        }
    }

    public static void crossOver(Route firstParent, Route secondParent) {
        Route firstChild = new Route();
        Route secondChild = new Route();

        Random rand = new Random();
        int endIndex = rand.nextInt(CITY_COUNT);

        for (int i = 0; i <= endIndex; i++) {
            firstChild.path.add(firstParent.path.get(i));
            secondChild.path.add(secondParent.path.get(i));
        }

        completeChildRoute(firstChild, secondParent, endIndex);
        completeChildRoute(secondChild, firstParent, endIndex);

        mutate(firstChild);
        mutate(secondChild);

        firstChild.calculateDistance(CITY_LONGITUDES, CITY_LATITUDES);
        secondChild.calculateDistance(CITY_LONGITUDES, CITY_LATITUDES);

        nextGenerationQueue.add(firstChild);
        nextGenerationQueue.add(secondChild);
    }

    public static void reproduce() {
        int initialSize = populationQueue.size();
        while (populationQueue.size() > initialSize / 2) {
            Route firstParent = populationQueue.poll();
            Route secondParent = populationQueue.poll();
            crossOver(firstParent, secondParent);

            nextGenerationQueue.add(firstParent);
            nextGenerationQueue.add(secondParent);
        }
        populationQueue.clear();
    }

    public static List<City> loadCitiesFromFile(String filename) throws IOException {
        List<City> cities = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filename + "_xy.csv"));
        List<String> names = Files.readAllLines(Paths.get(filename + "_name.csv"));

        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",");
            double longitude = Double.parseDouble(parts[0].trim());
            double latitude = Double.parseDouble(parts[1].trim());
            cities.add(new City(names.get(i).trim(), longitude, latitude));
        }
        return cities;
    }
}

class City {
    String name;
    double longitude;
    double latitude;

    City(String name, double longitude, double latitude) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}

class Route {
    List<Integer> path;
    double totalDistance;

    Route() {
        this.path = new ArrayList<>();
        this.totalDistance = 0;
    }

    public void calculateDistance(List<Double> cityLongitudes, List<Double> cityLatitudes) {
        this.totalDistance = 0;
        for (int i = 0; i < this.path.size() - 1; i++) {
            int fromCity = this.path.get(i);
            int toCity = this.path.get(i + 1);
            this.totalDistance += distance(cityLongitudes.get(fromCity), cityLatitudes.get(fromCity),
                    cityLongitudes.get(toCity), cityLatitudes.get(toCity));
        }
    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}
