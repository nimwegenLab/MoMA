import unittest
from pathlib import Path


class MyTestCase(unittest.TestCase):
    def test__get_top_level_paths__returns_deduplicated_list_of_parent_paths(self):
        from moma import get_top_level_paths
        paths = ["/folder1/",
                 "/folder1/subfolder/",
                 "/folder1/subfolder/",
                 "/folder1/anotherfolder/",
                 "/folder2/subfolder/",
                 "/folder2/anotherfolder/",
                 "/folder3/anotherfolder/",
                 "/folder3/anotherfolder/foo",
                 "/folder3/anotherfolder/bar",
                 "/folder3/anotherfolder/bar/folder1",
                 "/folder3/anotherfolder/bar/folder2",
                 "/folder3/anotherfolder/bar/folder3", ]
        paths = [Path(p) for p in paths]
        res = get_top_level_paths(paths)
        expected = [Path('/folder1'), Path('/folder2/subfolder'), Path('/folder3/anotherfolder'),
                    Path('/folder2/anotherfolder')]
        expected.sort()
        self.assertEqual(expected, res)

    def test__get_properties_path__using_p_option(self):
        from moma import get_properties_path
        prop_path = get_properties_path(["-p", "/test/path1/mm.properties"])
        self.assertEqual(Path("/test/path1/mm.properties"), prop_path)

    def test__get_properties_path__using_props_option_with_double_dash(self):
        from moma import get_properties_path
        prop_path = get_properties_path(["--props", "/test/path2/mm.properties"])
        self.assertEqual(Path("/test/path2/mm.properties"), prop_path)

    def test__get_properties_path__using_props_option_with_single_dash(self):
        from moma import get_properties_path
        prop_path = get_properties_path(["-props", "/test/path3/mm.properties"])
        self.assertEqual(Path("/test/path3/mm.properties"), prop_path)

    def test__get_properties_path__without_prop_AND_without_reload_option__returns_default_path(self):
        from moma import get_properties_path, default_mm_properties_path
        prop_path = get_properties_path([])
        self.assertEqual(default_mm_properties_path(), prop_path)

    def test__get_properties_path__with_reload_option__returns_empty_array(self):
        from moma import get_properties_path
        prop_path = get_properties_path(["-reload"])
        self.assertEqual(None, prop_path)


if __name__ == '__main__':
    unittest.main()
