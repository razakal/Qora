package kryo;

import difflib.Chunk;
import difflib.Delta;
import difflib.InsertDelta;

public class InsertDeltaSerializer extends AbstractDeltaSerializer{

	@Override
	public Delta<String> getObject(Chunk<String> original, Chunk<String> revised) {
		return new InsertDelta<String>(original, revised);
	}

}
