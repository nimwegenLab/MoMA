package com.jug.util.componenttree;

import com.jug.datahandling.IImageProvider;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import org.jetbrains.annotations.NotNull;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

/**
 * This class is a new version of {@link Component}. The goal is for all other code to depend on this class and remove
 * all dependencies for library implemenations of {@link Component} (such as e.g. MSER, FilteredComponent, etc.).
 * It can be created from another instance of {@link ComponentForest<C>}, while filtering test criteria with
 * {@link IComponentTester<T, C>}.
 *
 * @param <T> value type of the input image.
 * @author Michael Mell
 */
public final class AdvancedComponentForest<T extends Type<T>, C extends Component<T, C>>
        implements
        ComponentForest<AdvancedComponent<T>> {
    private ImgLabeling<Integer, IntType> labeling;
    private final List<AdvancedComponent<T>> nodes = new ArrayList<>();
    private final List<AdvancedComponent<T>> roots;
    private RandomAccessibleInterval<T> sourceImage;
    private Img<IntType> img;
    Integer label = 1;
    private int frame;
    private IComponentTester<T, C> tester;
    private ComponentProperties componentPropertiesCalculator;
    private IImageProvider imageProvider;


    public AdvancedComponentForest(ComponentForest<C> componentForest, RandomAccessibleInterval<T> sourceImage, int frame, IComponentTester<T, C> tester, ComponentProperties componentPropertiesCalculator, IImageProvider imageProvider) {
        this.imageProvider = imageProvider;
        roots = new ArrayList<>();
        this.sourceImage = sourceImage;
        this.frame = frame;
        this.tester = tester;
        this.componentPropertiesCalculator = componentPropertiesCalculator;
        CreateTree(componentForest);
        writeRootNodesToAllNodes();
        sortRootNodes();
        sortAllNodes();
        SortChildrenByPosition();
    }

    public AdvancedComponentForest(List<AdvancedComponent<T>> rootComponents) {
        roots = rootComponents;
        recursivelyAddNodes(roots);
        writeRootNodesToAllNodes();
        sortRootNodes();
        sortAllNodes();
        SortChildrenByPosition();
    }

    void recursivelyAddNodes(List<AdvancedComponent<T>> components) {
        for (AdvancedComponent<T> component : components) {
            nodes.add(component);
            recursivelyAddNodes(component.getChildren());
        }
    }

    private void writeRootNodesToAllNodes() {
        for (AdvancedComponent<T> node : nodes) {
            node.setComponentTreeRoots(roots);
        }
    }

    private void sortRootNodes() {
        ComponentPositionComparator positionComparator = new ComponentPositionComparator(1);
        roots.sort(positionComparator);
    }

    private void sortAllNodes() {
        ComponentPositionComparator positionComparator = new ComponentPositionComparator(1);
        nodes.sort(positionComparator);
    }

    private void SortChildrenByPosition() {
        for (final AdvancedComponent root : roots()) {
            SortChildrenRecursively(root);
        }
    }

    private void SortChildrenRecursively(AdvancedComponent parent) {
        List<AdvancedComponent<T>> children = parent.getChildren();
        ComponentPositionComparator positionComparator = new ComponentPositionComparator(1);
        children.sort(positionComparator);
        for (AdvancedComponent<T> component : children) {
            SortChildrenRecursively(component);
        }
    }

    private void CreateTree(ComponentForest<C> componentForest) {
        for (final C root : componentForest.roots()) {
            RecursivelyFindValidComponent(root);
        }
        for (AdvancedComponent<T> node : nodes) {
            if (node.getParent() == null) {
                roots.add(node);
            }
        }
    }

    private void RecursivelyFindValidComponent(C sourceComponent) {
        if (tester.IsValid(sourceComponent)) {
            AdvancedComponent<T> newRoot = new AdvancedComponent<>(label++, sourceComponent, sourceImage, componentPropertiesCalculator, frame, imageProvider); // TODO-MM-20220330: Is it a bug that we do not create a new labeling image per component? It seems to be because child components overlap ...
            nodes.add(newRoot);
            RecursivelyAddToTree(sourceComponent, newRoot);
        } else {
            if (tester.discontinueBranch()) {
                return;
            }
            for (final C sourceChildren : sourceComponent.getChildren()) {
                RecursivelyFindValidComponent(sourceChildren);
            }
        }
    }

    private void RecursivelyAddToTree(C sourceParent, AdvancedComponent<T> targetParent) {
        for (final C sourceChild : sourceParent.getChildren()) {
            if (tester.IsValid(sourceChild)) {  // if child meets condition, add it and with its children
                AdvancedComponent<T> targetChild = CreateTargetChild(targetParent, sourceChild);
                RecursivelyAddToTree(sourceChild, targetChild);
            } else {  // continue search for deeper component-nodes
                RecursivelyAddToTree(sourceChild, targetParent);
            }
        }
    }

    @NotNull
    private AdvancedComponent<T> CreateTargetChild(AdvancedComponent<T> targetComponent, C sourceChild) {
        AdvancedComponent<T> targetChild = new AdvancedComponent<>(label++, sourceChild, sourceImage, componentPropertiesCalculator, frame, imageProvider);
        targetChild.setParent(targetComponent);
        targetComponent.addChild(targetChild);
        nodes.add(targetChild);
        return targetChild;
    }

    @Override
    public Set<AdvancedComponent<T>> roots() {
        return new HashSet(roots);
    }

    public List<AdvancedComponent<T>> rootsSorted() {
        return roots;
    }

    public List<AdvancedComponent<T>> getAllComponents() {
        return nodes;
    }

    public AdvancedComponent<T> getComponentWithId(String targetId) {
        for (AdvancedComponent<T> component : getAllComponents()) {
            if (component.getStringId().equals(targetId)) {
                return component;
            }
        }
        return null;
    }

    public List<String> getComponentIds() {
        List<String> ids = new ArrayList<>();
        for (AdvancedComponent<T> component : getAllComponents()) {
            ids.add(component.getStringId());
        }
        return ids;
    }

    public RandomAccessibleInterval<T> getSourceImage() {
        return sourceImage;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof AdvancedComponentForest)) {
            return false;
        }
        boolean rootEqual = roots.equals(((AdvancedComponentForest<?, ?>) other).roots);
        boolean nodesEqual = nodes.equals(((AdvancedComponentForest<?, ?>) other).nodes);
//        Img<T> sourceImg1 = ImgView.wrap(sourceImage);
//        Img<T> sourceImg2 = ImgView.wrap(sourceImage);
//        boolean sourceImageEqual2 = sourceImg1.equals(sourceImg2);
//        boolean sourceImageEqual = sourceImage.equals(((AdvancedComponentForest<?, ?>) other).sourceImage);
        return rootEqual && nodesEqual;
    }
}

