package com.jug.util.componenttree;


import net.imglib2.Localizable;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.Type;
import net.imglib2.util.ValuePair;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class ComponentTester<T extends Type<T>, C extends Component<T, C>>
        implements
        IComponentTester<T, C> {

    private List<Function<Number, ? extends Number>> testMethods;

//    public ComponentTester(List<Function<Number, ? extends Number>> testMethods) {
//        this.testMethods = testMethods;
//    }
//public ComponentTester(List<Function<Number, ? extends Number>> testMethods) {
//    this.testMethods = testMethods;
//}

    @Override
    public boolean IsValid(C component) {
        int maxComponentWidth = 20;
        return ComponentWidth(component) <= maxComponentWidth;
    }

    private int ComponentWidth(Component component) {
        ValuePair<Integer, Integer> limits = getComponentLimits(component.iterator(), 0);
        return limits.b - limits.a;
    }

    private ValuePair<Integer, Integer> getComponentLimits(Iterator<Localizable> pixelPositionIterator, int dim) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        while (pixelPositionIterator.hasNext()) {
            Localizable location = pixelPositionIterator.next();
            final int pos = location.getIntPosition(dim);
            min = Math.min(min, pos);
            max = Math.max(max, pos);
        }
        return new ValuePair<>(min, max);
    }
}
