package kryo;

import difflib.Chunk;
import difflib.DeleteDelta;
import difflib.Delta;

public class DeleteDeltaSerializer extends AbstractDeltaSerializer{

	@Override
	public Delta<String> getObject(Chunk<String> original, Chunk<String> revised) {
		return new DeleteDelta<String>(original, revised);
	}

}
