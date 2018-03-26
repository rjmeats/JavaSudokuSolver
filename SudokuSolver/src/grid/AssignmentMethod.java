package grid;

/**
 * Different causes of a symbol being assigned to a cell while solving a Suduko puzzle.  
 */

public enum AssignmentMethod {

	NotAssigned, Given, Human, AutomatedDeduction, AutomatedBruteForce, Guess;
}
