import unittest

from app.services.embedding.composer import build_final_text, compute_source_hash


class ComposerTests(unittest.TestCase):
    def test_build_final_text_formats_sections(self):
        text = build_final_text(
            selected_tags=["여행", "사진", "여행"],
            user_description="  안녕하세요  ",
            image_captions=["바다", "바다", "산"],
        )
        self.assertIn("[SelectedTags]", text)
        self.assertIn("[UserDescription]", text)
        self.assertIn("[ImageCaptions]", text)
        self.assertIn("여행, 사진", text)
        self.assertIn("안녕하세요", text)
        self.assertIn("바다\n산", text)

    def test_compute_source_hash_is_stable(self):
        text = build_final_text(
            selected_tags=["a"],
            user_description="b",
            image_captions=["c"],
        )
        first = compute_source_hash(text)
        second = compute_source_hash(text)
        self.assertEqual(first, second)
        self.assertEqual(len(first), 64)


if __name__ == "__main__":
    unittest.main()
