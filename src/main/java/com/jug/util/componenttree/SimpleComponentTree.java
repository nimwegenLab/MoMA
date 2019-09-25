package com.jug.util.componenttree;

import com.jug.util.filteredcomponents.FilteredComponent;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class is a reduced version of {@link com.jug.util.filteredcomponents.FilteredComponentTree}.
 * It can be created from another instance of {@link ComponentForest<FilteredComponent< T >>}, while filtering
 * the {@link FilteredComponent}s that are to be included.
 *
 * @param <T> value type of the input image.
 * @author Michael Mell
 */
public final class SimpleComponentTree<T extends Type<T>, C extends Component<T, C>>
        implements
        ComponentForest<SimpleComponent<T, C>> {
    private final ArrayList<SimpleComponent<T, C>> nodes = new ArrayList<>();
    private final HashSet<SimpleComponent<T, C>> roots = new HashSet<>();
    private final RandomAccessibleInterval<T> sourceImage;
    private IComponentTester<T, C> tester;

    public SimpleComponentTree(ComponentForest<C> componentForest, RandomAccessibleInterval<T> sourceImage) {
        this.sourceImage = sourceImage;
        this.tester = new DummyComponentTester();
        CreateTree(componentForest);
    }

    public SimpleComponentTree(ComponentForest<C> componentForest, IComponentTester<T, C> tester, RandomAccessibleInterval<T> sourceImage) {
        this.sourceImage = sourceImage;
        this.tester = tester;
        CreateTree(componentForest);
    }

    private void CreateTree(ComponentForest<C> componentForest) {
        for (final C root : componentForest.roots()) {
            RecursivelyFindValidComponent(root);
        }
        for(SimpleComponent<T, C> node:nodes){
            if(node.getParent() == null)
            {
                roots.add(node);
            }
        }
    }

    private void RecursivelyFindValidComponent(C sourceComponent){
        if (tester.IsValid(sourceComponent)) {
            SimpleComponent<T, C> newRoot = new SimpleComponent<>(sourceComponent, sourceComponent.value(), sourceImage);
            nodes.add(newRoot);
            RecursivelyAddToTree(sourceComponent, newRoot);
        }
        else{
            for (final C sourceChildren : sourceComponent.getChildren()) {
                RecursivelyFindValidComponent(sourceChildren);
            }
        }
    }

    private void RecursivelyAddToTree(C sourceParent, SimpleComponent<T, C> targetParent) {
        for (final C sourceChild : sourceParent.getChildren()) {
            if (tester.IsValid(sourceChild)) {  // if child meets condition, add it and with its children
                SimpleComponent<T, C> targetChild = CreateTargetChild(targetParent, sourceChild);
                RecursivelyAddToTree(sourceChild, targetChild);
            } else {  // continue search for deeper component-nodes
                RecursivelyAddToTree(sourceChild, targetParent);
            }
        }
    }

    @NotNull
    private SimpleComponent<T, C> CreateTargetChild(SimpleComponent<T, C> targetComponent, C sourceChild) {
        SimpleComponent<T, C> targetChild = new SimpleComponent<>(sourceChild, sourceChild.value(), sourceImage);
        targetChild.setParent(targetComponent);
        targetComponent.addChild(targetChild);
        nodes.add(targetChild);
        return targetChild;
    }

    @Override
    public HashSet<SimpleComponent<T, C>> roots() {
        return roots;
    }
}

