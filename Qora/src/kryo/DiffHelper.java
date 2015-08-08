package kryo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import difflib.ChangeDelta;
import difflib.Chunk;
import difflib.DeleteDelta;
import difflib.DiffUtils;
import difflib.InsertDelta;
import difflib.Patch;
import difflib.PatchFailedException;

public class DiffHelper {

	public static String getDiff(String source, String destination)
			throws IOException {
		String[] src = StringUtils.split(source, "\n");
		String[] dest = StringUtils.split(destination, "\n");
		Patch<String> diff = DiffUtils.diff(Arrays.asList(src),
				Arrays.asList(dest));

		Kryo kryo = getkryo();

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				Output output = new Output(outputStream);) {
			kryo.writeObject(output, diff);
			output.close();
			byte[] byteArray = outputStream.toByteArray();
			return new String(byteArray);
		}

	}

	public static Kryo getkryo() {
		Kryo kryo = new Kryo();
		kryo.register(Chunk.class, new ChunkSerializer());
		kryo.register(ChangeDelta.class, new ChangeDeltaSerializer());
		kryo.register(InsertDelta.class, new InsertDeltaSerializer());
		kryo.register(DeleteDelta.class, new DeleteDeltaSerializer());
		return kryo;
	}

	public static String patch(String source, String patch) throws PatchFailedException {
		Kryo kryo = getkryo();
		try(Input input = new Input(patch.getBytes());)
		{
			@SuppressWarnings("unchecked")
			Patch<String> diff = kryo.readObject(input, Patch.class);
			input.close();
			String[] split = StringUtils.split(source, "\n");
			List<String> applyTo = diff.applyTo(Arrays.asList(split));
			String join = StringUtils.join(applyTo, "\n");
			
			return join;
		}

	}

}
