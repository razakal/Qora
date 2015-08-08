package kryo;

import difflib.ChangeDelta;
import difflib.Chunk;
import difflib.Delta;

public class ChangeDeltaSerializer extends AbstractDeltaSerializer {

	@Override
	public Delta<String> getObject(Chunk<String> original, Chunk<String> revised) {
		return new ChangeDelta<String>(original, revised);
	}

}
