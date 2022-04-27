package com.ddf.materialbintool.materials.definition;

public enum InterpolationConstraint {
    FLAT,
    SMOOTH,
	NOPERSPECTIVE,
	CENTROID;

    public static InterpolationConstraint get(int i) {
        return values()[i];
    }
}
