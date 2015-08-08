package kryo;

import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import difflib.Chunk;

public class ChunkSerializer extends Serializer<Chunk<String>> {

	@Override
	public void write(Kryo kryo, Output output, Chunk<String> object) {
		output.writeInt(object.getPosition());
		List<String> lines = object.getLines();
		output.writeInt(lines.size());
		for (String string : lines) {
			output.writeString(string);
		}

	}

	@Override
	public Chunk<String> read(Kryo kryo, Input input, Class<Chunk<String>> type) {

		int position = input.readInt();
		int linessize = input.readInt();
		List<String> lines = new ArrayList<String>();
		for (int i = 0; i < linessize; i++) {
			lines.add(input.readString());
		}

		return new Chunk<String>(position, lines);
	}

}
