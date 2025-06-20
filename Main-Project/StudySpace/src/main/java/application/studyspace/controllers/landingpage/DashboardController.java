package application.studyspace.controllers.landingpage;

import application.studyspace.services.Scenes.ViewManager;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.addons.HappinessIndicator;
import eu.hansolo.tilesfx.addons.Indicator;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.chart.SmoothedChart;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import java.time.Duration;

public class DashboardController {

    @FXML
    private Tile totalProgressTile, lineChartTile, PreferredStudyHoursTile;

    @FXML
    private StackPane box1, box2, box3, box4, box5, box6, box7;

    @FXML
    private void initialize() {
        // Progress Bar -- BOX 1 --
        totalProgressTile.setValue(75); // Set progress to 75%
        totalProgressTile.setBackgroundColor((Color) Paint.valueOf("WHITE"));
        totalProgressTile.setValueColor((Color) Paint.valueOf("BLACK"));
        box1.setPadding(new Insets(0, 0, 20, 0));

        // Chart -- BOX 2 --
        XYChart.Series<String, Number> series2 = new XYChart.Series();
        series2.setName("Inside");
        series2.getData().add(new XYChart.Data<>("MO", 8));
        series2.getData().add(new XYChart.Data<>("TU", 5));
        series2.getData().add(new XYChart.Data<>("WE", 0));
        series2.getData().add(new XYChart.Data<>("TH", 2));
        series2.getData().add(new XYChart.Data<>("FR", 4));
        series2.getData().add(new XYChart.Data<>("SA", 3));
        series2.getData().add(new XYChart.Data<>("SU", 5));

        XYChart.Series<String, Number> series3 = new XYChart.Series();
        series3.setName("Outside");
        series3.getData().add(new XYChart.Data<>("MO", 8));
        series3.getData().add(new XYChart.Data<>("TU", 5));
        series3.getData().add(new XYChart.Data<>("WE", 0));
        series3.getData().add(new XYChart.Data<>("TH", 2));
        series3.getData().add(new XYChart.Data<>("FR", 4));
        series3.getData().add(new XYChart.Data<>("SA", 3));
        series3.getData().add(new XYChart.Data<>("SU", 5));

        lineChartTile = TileBuilder.create()
                .skinType(Tile.SkinType.SMOOTHED_CHART)
                .title("SmoothedChart Tile")
                .animated(true)
                .smoothing(false)
                .series(series2, series3)
                .titleColor(Color.BLACK)
                .backgroundColor(Color.WHITE)
                .valueColor(Color.BLACK)
                .chartGridColor(Color.LIGHTGRAY)
                .smoothing(true)
                .build();

        box5.getChildren().add(lineChartTile);

        lineChartTile.getXAxis().setTickLabelFill(Color.BLACK);
        lineChartTile.getYAxis().setTickLabelFill(Color.BLACK);

        //Chart -- Box3 --
        Tile countdownTile = TileBuilder.create()
                .skinType(Tile.SkinType.COUNTDOWN_TIMER)
                .title("Time Left")
                .text("... left until the next exam")
                .timePeriod(Duration.ofSeconds(10))
                .running(false)
                .backgroundColor(Color.WHITE)
                .barColor(Color.BLACK)
                .titleColor(Color.BLACK)
                .valueColor(Color.BLACK)
                .textColor(Color.BLACK)
                .build();

        countdownTile.setRunning(true);

        box3.getChildren().add(countdownTile);

        //Chart -- Box4 --
        XYChart.Series<String, Number> series4 = new XYChart.Series();
        series4.setName("Inside");
        series4.getData().add(new XYChart.Data<>("06 AM", 8));
        series4.getData().add(new XYChart.Data<>("07 AM", 5));
        series4.getData().add(new XYChart.Data<>("08 AM", 0));
        series4.getData().add(new XYChart.Data<>("09 AM", 2));
        series4.getData().add(new XYChart.Data<>("10 AM", 4));
        series4.getData().add(new XYChart.Data<>("11 AM", 3));
        series4.getData().add(new XYChart.Data<>("12 AM", 5));
        series4.getData().add(new XYChart.Data<>("01 PM", 8));
        series4.getData().add(new XYChart.Data<>("02 PM", 5));
        series4.getData().add(new XYChart.Data<>("03 PM", 0));
        series4.getData().add(new XYChart.Data<>("04 PM", 2));
        series4.getData().add(new XYChart.Data<>("05 PM", 4));
        series4.getData().add(new XYChart.Data<>("06 PM", 3));
        series4.getData().add(new XYChart.Data<>("07 PM", 5));

        XYChart.Series<String, Number> series5 = new XYChart.Series();
        series5.setName("Outside");
        series5.getData().add(new XYChart.Data<>("06 AM", 8));
        series5.getData().add(new XYChart.Data<>("07 AM", 5));
        series5.getData().add(new XYChart.Data<>("08 AM", 0));
        series5.getData().add(new XYChart.Data<>("09 AM", 2));
        series5.getData().add(new XYChart.Data<>("10 AM", 4));
        series5.getData().add(new XYChart.Data<>("11 AM", 3));
        series5.getData().add(new XYChart.Data<>("12 AM", 5));
        series5.getData().add(new XYChart.Data<>("01 PM", 8));
        series5.getData().add(new XYChart.Data<>("02 PM", 5));
        series5.getData().add(new XYChart.Data<>("03 PM", 0));
        series5.getData().add(new XYChart.Data<>("04 PM", 2));
        series5.getData().add(new XYChart.Data<>("05 PM", 4));
        series5.getData().add(new XYChart.Data<>("06 PM", 3));
        series5.getData().add(new XYChart.Data<>("07 PM", 5));

        lineChartTile = TileBuilder.create()
                .skinType(Tile.SkinType.SMOOTHED_CHART)
                .title("SmoothedChart Tile")
                .animated(true)
                .smoothing(false)
                .series(series4, series5)
                .titleColor(Color.BLACK)
                .backgroundColor(Color.WHITE)
                .valueColor(Color.BLACK)
                .chartGridColor(Color.LIGHTGRAY)
                .build();

        box4.getChildren().add(lineChartTile);

       lineChartTile.getXAxis().setTickLabelFill(Color.BLACK);
       lineChartTile.getYAxis().setTickLabelFill(Color.BLACK);

        //Chart -- Box5 --
        HappinessIndicator happy   = new HappinessIndicator(HappinessIndicator.Happiness.HAPPY, 0.67);
        HappinessIndicator neutral = new HappinessIndicator(HappinessIndicator.Happiness.NEUTRAL, 0.25);
        HappinessIndicator unhappy = new HappinessIndicator(HappinessIndicator.Happiness.UNHAPPY, 0.08);

        happy.setBarBackgroundColor(Color.LIGHTGRAY);
        neutral.setBarBackgroundColor(Color.LIGHTGRAY);
        unhappy.setBarBackgroundColor(Color.LIGHTGRAY);
        happy.setTextColor(Color.BLACK);
        neutral.setTextColor(Color.BLACK);
        unhappy.setTextColor(Color.BLACK);

        HBox happiness = new HBox(unhappy, neutral, happy);
        happiness.setFillHeight(true);

        HBox.setHgrow(happy, Priority.ALWAYS);
        HBox.setHgrow(neutral, Priority.ALWAYS);
        HBox.setHgrow(unhappy, Priority.ALWAYS);

        Tile happinessTile = TileBuilder.create()
                .skinType(Tile.SkinType.CUSTOM)
                .title("Concentration Index")
                .textVisible(true)
                .graphic(happiness)
                .value(0)
                .animated(true)
                .backgroundColor(Color.WHITE)
                .titleColor(Color.BLACK)
                .textColor(Color.BLACK)
                .chartGridColor(Color.BLACK)
                .barColor(Color.BLACK)
                .barBackgroundColor(Color.WHITE)
                .valueColor(Color.BLACK)
                .build();


        box2.getChildren().add(happinessTile);

        //Chart -- Box6 --
        ChartData chartData1 = new ChartData("Math", 24.0, Tile.GREEN);
        ChartData chartData2 = new ChartData("Physics", 10.0, Tile.BLUE);
        ChartData chartData3 = new ChartData("German", 12.0, Tile.RED);
        ChartData chartData4 = new ChartData("Law", 13.0, Tile.YELLOW_ORANGE);
        ChartData chartData5 = new ChartData("Item 5", 13.0, Tile.BLUE);
        ChartData chartData6 = new ChartData("Item 6", 13.0, Tile.BLUE);
        ChartData chartData7 = new ChartData("Item 7", 13.0, Tile.BLUE);
        ChartData chartData8 = new ChartData("Item 8", 13.0, Tile.BLUE);

        Tile radialChartTile = TileBuilder.create()
                .skinType(Tile.SkinType.RADIAL_CHART)
                .title("Progress by Subject")
                .textVisible(true)
                .chartData(chartData1, chartData2, chartData3, chartData4)
                .backgroundColor(Color.WHITE)
                .valueColor(Color.BLACK)
                .titleColor(Color.BLACK)
                .textColor(Color.BLACK)
                .descriptionColor(Color.BLACK)
                .build();

        box6.setAlignment(Pos.CENTER);

        radialChartTile.setTranslateX(0);
        radialChartTile.setTranslateY(0);
        radialChartTile.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // Allow the tile to expand
        radialChartTile.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE); // Default size
        StackPane.setMargin(radialChartTile, Insets.EMPTY);
        StackPane.setMargin(radialChartTile, new Insets(0, 0, 0, 50)); // Top, Right, Bottom,


        box6.getChildren().clear();
        box6.getChildren().add(radialChartTile);

        //Notification -- Box7 --
        Indicator leftGraphics = new Indicator(Tile.RED);
        leftGraphics.setOn(true);

        Indicator middleGraphics = new Indicator(Tile.YELLOW);
        middleGraphics.setOn(true);

        Indicator rightGraphics = new Indicator(Tile.GREEN);
        rightGraphics.setOn(true);

        Tile statusTile = TileBuilder.create()
                .skinType(Tile.SkinType.STATUS)
                .title("Status of Sessions")
                .description("This indicates your progress in the Study Sessions based on the Feedback you provided.")
                .leftText("Sessions Missed")
                .middleText("Sessions without Feedback")
                .rightText("Sessions Completed")
                .leftGraphics(leftGraphics)
                .middleGraphics(middleGraphics)
                .rightGraphics(rightGraphics)
                .backgroundColor(Color.WHITE)
                .textColor(Color.BLACK)
                .titleColor(Color.BLACK)
                .foregroundColor(Color.BLACK)
                .build();

        box7.getChildren().add(statusTile);
    }


    @FXML private void handleSidebarCalendar(ActionEvent event) {
        ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
    }

    @FXML private void handleSidebarDashboard(ActionEvent event) {
        // Already on Dashboard
    }

    @FXML private void handleSidebarSettings(ActionEvent event) {
        ViewManager.show("/application/studyspace/landingpage/Settings.fxml");
    }

    private SmoothedChart findSmoothedChart(Node node) {
        if (node instanceof SmoothedChart) {
            return (SmoothedChart) node;
        }
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                SmoothedChart result = findSmoothedChart(child);
                if (result != null) return result;
            }
        }
        return null;
    }
}
