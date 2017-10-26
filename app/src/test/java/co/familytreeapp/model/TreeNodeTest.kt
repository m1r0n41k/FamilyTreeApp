package co.familytreeapp.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [TreeNode].
 */
class TreeNodeTest {

    @Test
    fun multipleChildrenAddedOk() {
        val node = TreeNode("Parent")
        node.addChildren(listOf(
                TreeNode("Child 1"),
                TreeNode("Child 2"),
                TreeNode("Child 3")
        ))

        assertEquals(node.getChildren().size, 3)
    }

    @Test
    fun childNodeParentUpdatedWhenAddedToNode() {
        val node = TreeNode("Parent")
        node.addChild(TreeNode("Child"))
        val child = node.getChildren()[0]

        assertNotNull(child.parent)
        assertEquals(child.parent!!.data, "Parent")
    }

    @Test
    fun checkChildrenAdded() {
        val root = TreeNode("Root")
        root.addChild(TreeNode("Albert"))
        root.addChild(TreeNode("Bethany"))

        val child3 = TreeNode("Camilla")
        child3.addChild(TreeNode("Daniel"))
        child3.addChild(TreeNode("Edmund"))
        root.addChild(child3)

        assertEquals(root.getChildren().size, 3)
        assertEquals(child3.getChildren().size, 2)
    }

    @Test
    fun rootsAndLeavesRecognised() {
        val root = TreeNode(34)
        val child1 = TreeNode(5)
        root.addChild(child1)

        val child2 = TreeNode(4)
        val grandChild1 = TreeNode(24)
        val grandChild2 = TreeNode(12)
        child2.addChild(grandChild1)
        child2.addChild(grandChild2)
        root.addChild(child2)

        assertTrue(root.isRoot())
        assertTrue(child1.isLeaf())
        assertFalse(child2.isLeaf())
        assertTrue(grandChild1.isLeaf())
        assertTrue(grandChild2.isLeaf())
    }

    @Test
    fun correctConversionToTreeList() {
        val dummyRootNode = TreeNode("Grandparent")

        val parent1 = TreeNode("Parent 1")
        val child1 = TreeNode("Child 1")
        val child2 = TreeNode("Child 2")
        child2.addChild(TreeNode("Grandchild 1"))
        val child3 = TreeNode("Child 3")
        parent1.addChildren(listOf(child1, child2, child3))

        val parent2 = TreeNode("Parent 2")
        parent2.addChild(TreeNode("Child 4"))

        val parent3 = TreeNode("Parent 3")

        dummyRootNode.addChildren(listOf(parent1, parent2, parent3))

        val expectedList = ArrayList<TreeListItem<String>>()
        with(expectedList) {
            add(TreeListItem("Grandparent", 0))
            add(TreeListItem("Parent 1", 1))
            add(TreeListItem("Child 1", 2))
            add(TreeListItem("Child 2", 2))
            add(TreeListItem("Grandchild 1", 3))
            add(TreeListItem("Child 3", 2))
            add(TreeListItem("Parent 2", 1))
            add(TreeListItem("Child 4", 2))
            add(TreeListItem("Parent 3", 1))
        }

        assertEquals(dummyRootNode.asTreeList(), expectedList)
    }

}
