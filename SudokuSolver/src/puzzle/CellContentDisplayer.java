package puzzle;

import solver.*;

public interface CellContentDisplayer {

	String getContent(CellAssessment ca, boolean Highlight);
	String getHeading();
}
