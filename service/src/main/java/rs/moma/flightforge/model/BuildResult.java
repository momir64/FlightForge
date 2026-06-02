package rs.moma.flightforge.model;

import java.util.List;

public record BuildResult(BuildConfig build, List<BuildWarning> warnings) {}