package celemendi.gracia.david.problems;

public interface Solver<P extends ComputationalProblem> {

    void compute(P computationalProblem);

}
