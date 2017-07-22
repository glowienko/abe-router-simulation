package utils;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProbabilityDistribution {

    private Enums.TYPE type;
    private double lambda;

    /**
     * Gets packet arrival time [ms] or packet length [bits]
     * according to poisson distribution
     * @param y - value from 0 to 1 which is generated randomly
     *          - its y axis on the probability distribution graph
     * @return
     */
    public double getPoissonValue(double y) {
        return ((-1) * Math.log(1 - y) * lambda); //strumien poissona : Fx = 1-e^(-lambda*x)
        //jak damy / lambda zamist * lambda to przy lambda 1000 będie generowało jakkies 1*e-5, e-4
    }
}
