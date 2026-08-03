from pathlib import Path
import subprocess


REPOSITORY_ROOT = Path(__file__).resolve().parents[2]


def test_public_monorepo_boundaries_exist():
    assert (REPOSITORY_ROOT / "android").is_dir()
    assert (REPOSITORY_ROOT / "gateway").is_dir()
    assert (REPOSITORY_ROOT / "docs" / "api-contract.md").is_file()


def test_private_and_build_artifacts_are_ignored():
    gitignore = (REPOSITORY_ROOT / ".gitignore").read_text(encoding="utf-8")

    for rule in (".env", "*.db", "*.sqlite", "*.apk", "tailscale", "google"):
        assert rule in gitignore


def test_no_private_files_are_tracked():
    tracked = subprocess.check_output(
        ["git", "ls-files"], cwd=REPOSITORY_ROOT, text=True
    ).splitlines()
    forbidden = (".env", ".db", ".sqlite", ".apk", "tailscale", "token")

    assert not [
        path for path in tracked
        if any(part in path.lower() for part in forbidden)
        and not path.lower().endswith(".env.example")
        and path.lower() != "docs/tailscale-setup.md"
    ]


if __name__ == "__main__":
    test_public_monorepo_boundaries_exist()
    test_private_and_build_artifacts_are_ignored()
    test_no_private_files_are_tracked()
    print("repository hygiene checks passed")
