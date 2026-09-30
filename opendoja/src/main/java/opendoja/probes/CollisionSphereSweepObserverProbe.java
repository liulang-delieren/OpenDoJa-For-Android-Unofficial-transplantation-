package opendoja.probes;

import com.nttdocomo.ui.graphics3d.collision.Collision;
import com.nttdocomo.ui.graphics3d.collision.CollisionObserver;
import com.nttdocomo.ui.graphics3d.collision.Shape;
import com.nttdocomo.ui.graphics3d.collision.Sphere;
import com.nttdocomo.ui.util3d.Vector3D;

public final class CollisionSphereSweepObserverProbe {
    private static final float EPSILON = 0.0001f;

    private CollisionSphereSweepObserverProbe() {
    }

    public static void main(String[] args) {
        DemoLog.enableInfoLogging();

        Collision collision = new Collision();
        CapturingObserver observer = new CapturingObserver();
        collision.setObserver(observer);

        Sphere target = new Sphere(2f);
        target.setCenter(new Vector3D(0f, 0f, 0f));

        Sphere moving = new Sphere(1f);
        moving.setCenter(new Vector3D(-10f, 0f, 0f));

        boolean hit = collision.isHit(target, moving, new Vector3D(1f, 0f, 0f), true);
        if (!hit) {
            throw new IllegalStateException("Expected swept sphere hit");
        }
        if (observer.calls != 1) {
            throw new IllegalStateException("Expected exactly one collision callback, got " + observer.calls);
        }
        assertApprox("contactPos", observer.contactPos, 7f / 11f);
        assertApprox("distance", observer.distance, 1f);
        assertVector("normal", observer.normal, -1f, 0f, 0f);

        DemoLog.info(CollisionSphereSweepObserverProbe.class,
                String.format("contactPos=%.6f distance=%.6f normal=(%.1f,%.1f,%.1f)",
                        observer.contactPos,
                        observer.distance,
                        observer.normal.getX(),
                        observer.normal.getY(),
                        observer.normal.getZ()));
    }

    private static void assertApprox(String label, float actual, float expected) {
        if (Math.abs(actual - expected) > EPSILON) {
            throw new IllegalStateException(label + " expected " + expected + " but was " + actual);
        }
    }

    private static void assertVector(String label, Vector3D actual, float expectedX, float expectedY, float expectedZ) {
        if (actual == null) {
            throw new IllegalStateException(label + " was null");
        }
        assertApprox(label + ".x", actual.getX(), expectedX);
        assertApprox(label + ".y", actual.getY(), expectedY);
        assertApprox(label + ".z", actual.getZ(), expectedZ);
    }

    private static final class CapturingObserver implements CollisionObserver {
        private int calls;
        private float contactPos;
        private float distance;
        private final Vector3D normal = new Vector3D();

        @Override
        public void onHit(Shape left, Shape right, boolean fromBackFace, Vector3D point) {
        }

        @Override
        public boolean onHit(Shape shape, int count, com.nttdocomo.ui.graphics3d.collision.BoundingVolume[] volumes,
                             int[] boneIds, boolean[] fromBackFace, Vector3D[] points) {
            return false;
        }

        @Override
        public void onHit(Shape shape, Sphere sphere, float contactPos, Vector3D normal, float distance) {
            calls++;
            this.contactPos = contactPos;
            this.distance = distance;
            this.normal.set(normal);
        }

        @Override
        public void onPick(com.nttdocomo.ui.graphics3d.collision.Ray ray,
                           com.nttdocomo.ui.graphics3d.Figure figure,
                           com.nttdocomo.ui.graphics3d.collision.IntersectionAttribute[] attributes) {
        }
    }
}
