"""Allow duplicate group names by removing unique constraint."""

from alembic import op

# revision identifiers, used by Alembic.
revision = "0002_drop_group_name_unique"
down_revision = "0001_add_group_embedding"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.drop_constraint("groups_name_key", "groups", type_="unique")


def downgrade() -> None:
    op.create_unique_constraint("groups_name_key", "groups", ["name"])
