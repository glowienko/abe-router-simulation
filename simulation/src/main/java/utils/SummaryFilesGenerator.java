package utils;


import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

@NoArgsConstructor
public class SummaryFilesGenerator {


    private static final String FILES_BASIC_PATH = "C:/Users/jaroslaw/Documents/studia/sem 6/swus/projekt/abe/stats/";

    private static final String BEST_EFFORT_STATS_FILE_NAME = "best_effort_statistics";
    private static final String BEST_EFFORT_VALUES_TO_CHART_FILE_NAME = "best_effort_chart_values";

    private static final String BETTER_THAN_BEST_EFFORT_STATS_FILE_NAME = "better_than_best_effort_statistics";
    private static final String BETTER_THAN_BEST_EFFORT_VALUES_TO_CHART_FILE_NAME = "better_than_best_effort_chart_values";

    private static final String ALL_X_AXIS = "_all_timeXaxis";
    private static final String BLUE_X_AXIS = "_blue_timeXaxis";
    private static final String GREEN_X_AXIS = "_green_timeXaxis";

    private static final String ALL_Y_AXIS_DELAY = "_all_Yaxis_delay";
    private static final String BLUE_Y_AXIS_DELAY = "blue_Yaxis_delay";
    private static final String GREEN_Y_AXIS_DELAY = "_green_Yaxis_delay";


    public void createFiles(TrafficStatistics statistics, Enums.SIMULATION_TYPE simulationType) {
        saveSimpleStatisticsToFile(statistics, simulationType);
        saveChartDataToFiles(statistics, simulationType);
    }

    @SneakyThrows(FileNotFoundException.class)
    private void saveSimpleStatisticsToFile(TrafficStatistics statistics, Enums.SIMULATION_TYPE simulationType) {
        PrintWriter statsFileStream;

        if (simulationType.equals(Enums.SIMULATION_TYPE.best_effort)) {
            File file = new File(FILES_BASIC_PATH + BEST_EFFORT_STATS_FILE_NAME + ".txt");
            statsFileStream = new PrintWriter(file);
        } else {
            File file = new File(FILES_BASIC_PATH + BETTER_THAN_BEST_EFFORT_STATS_FILE_NAME + ".txt");
            statsFileStream = new PrintWriter(file);
        }

        statsFileStream.println("Virtual Queue Occupancy: " + statistics.countVirtualQueueOccupancy());
        statsFileStream.println("Number of packets: " + statistics.getTotalPacketsNumber());
        statsFileStream.println("Number of dropped packets: " + statistics.getTotalPacketsLostNumber());
        statsFileStream.println("============================================");
        statsFileStream.println("Percentage of dropped packets: " + statistics.countAvgPacketLoss() + " %");
        statsFileStream.println("Average transfer delay: " + statistics.countAvgPacketDelay());

       // if (simulationType.equals(Enums.SIMULATION_TYPE.better_than_best_effort)) {
            statsFileStream.println("Number of blue packets: " + statistics.getBluePacketsNumber());
            statsFileStream.println("Number of green packets: " + statistics.getGreenPacketsNumber());
            statsFileStream.println("============================================");
            statsFileStream.println("Number of dropped blue packets: " + statistics.getBluePacketsLostNumber());
            statsFileStream.println("Number of dropped green packets: " + statistics.getGreenPacketsLostNumber());
            statsFileStream.println("============================================");
            statsFileStream.println("Percentage of dropped blue packets: " + statistics.countAvgBluePacketLoss() + " %");
            statsFileStream.println("Percentage of dropped green packets: " + statistics.countAvgGreenPacketLoss() + " %");
            statsFileStream.println("============================================");
            statsFileStream.println("Average transfer delay of blue packets: " + statistics.countAvgBluePacketDelay());
            statsFileStream.println("Average transfer delay of green packets: " + statistics.countAvgGreenPacketDelay());
        //}
        statsFileStream.close();
    }

    @SneakyThrows(FileNotFoundException.class)
    private void saveChartDataToFiles(TrafficStatistics statistics, Enums.SIMULATION_TYPE simulationType) {
        PrintWriter statsFileStream;

        // all packets x axis times file
        if (simulationType.equals(Enums.SIMULATION_TYPE.best_effort)) {
            File file = new File(FILES_BASIC_PATH + BEST_EFFORT_VALUES_TO_CHART_FILE_NAME + ALL_X_AXIS + ".txt");
            statsFileStream = new PrintWriter(file);
        } else {
            File file = new File(FILES_BASIC_PATH + BETTER_THAN_BEST_EFFORT_VALUES_TO_CHART_FILE_NAME  + ALL_X_AXIS + ".txt");
            statsFileStream = new PrintWriter(file);
        }
        statistics.timeXAxis.forEach(statsFileStream::println);
        statsFileStream.close();

        //all packets y axis delays file
        if (simulationType.equals(Enums.SIMULATION_TYPE.best_effort)) {
            File file = new File(FILES_BASIC_PATH + BEST_EFFORT_VALUES_TO_CHART_FILE_NAME  + ALL_Y_AXIS_DELAY + ".txt");
            statsFileStream = new PrintWriter(file);
        } else {
            File file = new File(FILES_BASIC_PATH + BETTER_THAN_BEST_EFFORT_VALUES_TO_CHART_FILE_NAME  + ALL_Y_AXIS_DELAY + ".txt");
            statsFileStream = new PrintWriter(file);
        }
        statistics.delaysYAxis.forEach(statsFileStream::println);
        statsFileStream.close();


        //green packets x axis times file
        if (simulationType.equals(Enums.SIMULATION_TYPE.better_than_best_effort)) {
            File file = new File(FILES_BASIC_PATH + BETTER_THAN_BEST_EFFORT_VALUES_TO_CHART_FILE_NAME + GREEN_X_AXIS + ".txt");
            statsFileStream = new PrintWriter(file);

            statistics.timeXAxisGreen.forEach(statsFileStream::println);
            statsFileStream.close();

        }
        //green packets y axis delay file
        if (simulationType.equals(Enums.SIMULATION_TYPE.better_than_best_effort)) {
            File file = new File(FILES_BASIC_PATH + BETTER_THAN_BEST_EFFORT_VALUES_TO_CHART_FILE_NAME + GREEN_Y_AXIS_DELAY + ".txt");
            statsFileStream = new PrintWriter(file);

            statistics.delaysYAxisGreen.forEach(statsFileStream::println);
            statsFileStream.close();

        }

        //blue packets x axis times file
        if (simulationType.equals(Enums.SIMULATION_TYPE.best_effort)) {
            File file = new File(FILES_BASIC_PATH + BEST_EFFORT_VALUES_TO_CHART_FILE_NAME  + BLUE_X_AXIS + ".txt");
            statsFileStream = new PrintWriter(file);
        } else {
            File file = new File(FILES_BASIC_PATH + BETTER_THAN_BEST_EFFORT_VALUES_TO_CHART_FILE_NAME  + BLUE_X_AXIS + ".txt");
            statsFileStream = new PrintWriter(file);
        }
        statistics.timeXAxisBlue.forEach(statsFileStream::println);
        statsFileStream.close();



        //blue packets y axis delay file
        if (simulationType.equals(Enums.SIMULATION_TYPE.best_effort)) {
            File file = new File(FILES_BASIC_PATH + BEST_EFFORT_VALUES_TO_CHART_FILE_NAME  + BLUE_Y_AXIS_DELAY + ".txt");
            statsFileStream = new PrintWriter(file);
        } else {
            File file = new File(FILES_BASIC_PATH + BETTER_THAN_BEST_EFFORT_VALUES_TO_CHART_FILE_NAME  + BLUE_Y_AXIS_DELAY + ".txt");
            statsFileStream = new PrintWriter(file);
        }
        statistics.delaysYAxisBlue.forEach(statsFileStream::println);
        statsFileStream.close();
    }

}
