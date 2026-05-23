package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildWarning {
    private BuildWarningType type;
    private String message;
    private String relatedComponent;
    private BuildConfig build;
}
