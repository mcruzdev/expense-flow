package guru.quarkus.expense.data;

import java.net.URL;

public record PreSignedURLResponse(URL preSignedURL, String correlationID) {
}
