import javax.swing.*;
import java.awt.*;
import java.util.List;

class Visualizer extends JPanel {

    private final java.util.List<City> cities;
    private final java.util.List<Integer> path;

    public Visualizer(java.util.List<City> cities, java.util.List<Integer> path) {
        this.cities = cities;
        this.path = path;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth()-100;
        int height = getHeight()-100;

        double minLongitude = GeneticAlgorithm.CITY_LONGITUDES.stream().min(Double::compareTo).orElse(0.0);
        double maxLongitude = GeneticAlgorithm.CITY_LONGITUDES.stream().max(Double::compareTo).orElse(1.0);
        double minLatitude = GeneticAlgorithm.CITY_LATITUDES.stream().min(Double::compareTo).orElse(0.0);
        double maxLatitude = GeneticAlgorithm.CITY_LATITUDES.stream().max(Double::compareTo).orElse(1.0);

        int[] xCoords = new int[cities.size()];
        int[] yCoords = new int[cities.size()];
        for (int i = 0; i < cities.size(); i++) {
            xCoords[i] = (int) ((GeneticAlgorithm.CITY_LONGITUDES.get(i) - minLongitude) / (maxLongitude - minLongitude) * (width - 50) + 25);
            yCoords[i] = (int) ((GeneticAlgorithm.CITY_LATITUDES.get(i) - minLatitude) / (maxLatitude - minLatitude) * (height - 50) + 25);
        }

        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < path.size() - 1; i++) {
            int from = path.get(i);
            int to = path.get(i + 1);
            g2d.drawLine(xCoords[from], yCoords[from], xCoords[to], yCoords[to]);
        }

        g2d.setColor(Color.RED);
        for (int i = 0; i < cities.size(); i++) {
            g2d.fillOval(xCoords[i] - 5, yCoords[i] - 5, 10, 10);
            g2d.drawString(cities.get(i).name, xCoords[i] + 10, yCoords[i] + 20);
        }
    }

    public static void display(java.util.List<City> cities, List<Integer> path) {
        JFrame frame = new JFrame("Shortest Path Visualization");
        Visualizer visualizer = new Visualizer(cities, path);
        frame.add(visualizer);
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
