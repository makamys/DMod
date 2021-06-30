package makamys.dmod;

import java.util.UUID;

public class DUtil {
	public static UUID UUIDorNullFromString(String str) {
		return str == null || str.isEmpty() ? null : UUID.fromString(str);
	}
}
