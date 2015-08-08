package kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import difflib.Chunk;
import difflib.Delta;

public abstract class AbstractDeltaSerializer extends Serializer<Delta<String>>{

	@Override
	public void write(Kryo kryo, Output output, Delta<String> object) {
		
		kryo.writeClassAndObject(output, object.getOriginal());
		kryo.writeClassAndObject(output, object.getRevised());
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Delta<String> read(Kryo kryo, Input input, Class<Delta<String>> type) {
		Chunk<String> original = (Chunk<String>) kryo.readClassAndObject(input);
		Chunk<String> revised = (Chunk<String>) kryo.readClassAndObject(input);
		return getObject(original, revised);
	}
	
	
	public abstract Delta<String> getObject(Chunk<String> original, Chunk<String> revised);
}
