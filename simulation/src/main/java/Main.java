import algorithm.BestEffort;
import algorithm.BetterThanBestEffort;
import utils.Enums;
import utils.SummaryFilesGenerator;
import utils.TrafficStatistics;

public class Main {
    public static void main(String args[]) {

        BetterThanBestEffort betterThanBestEffort = new BetterThanBestEffort();
        BestEffort bestEffort = new BestEffort();

        TrafficStatistics bestEffortStats;
        TrafficStatistics betterThanBestEffortStats;

        SummaryFilesGenerator filesGenerator = new SummaryFilesGenerator();

        System.out.println("started");

        betterThanBestEffortStats = betterThanBestEffort.startSimulation();
        bestEffortStats = bestEffort.startSimulation();

        System.out.println("finished");

        filesGenerator.createFiles(betterThanBestEffortStats, Enums.SIMULATION_TYPE.better_than_best_effort);
        filesGenerator.createFiles(bestEffortStats, Enums.SIMULATION_TYPE.best_effort);
    }
}
