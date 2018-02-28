package puzzle;
import grid.CellAssessment;

public interface CellContentDisplayer {

	String getContent(CellAssessment ca, boolean Highlight);
//	String getContent(Cell c, boolean Highlight);
	String getHeading();
}
