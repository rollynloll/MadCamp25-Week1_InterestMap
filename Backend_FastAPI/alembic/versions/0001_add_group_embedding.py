"""Add group embedding columns."""

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

# revision identifiers, used by Alembic.
revision = "0001_add_group_embedding"
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.add_column(
        "groups",
        sa.Column("embedding", postgresql.JSONB(), nullable=True),
    )
    op.add_column(
        "groups",
        sa.Column("embedding_updated_at", sa.TIMESTAMP(timezone=True), nullable=True),
    )


def downgrade() -> None:
    op.drop_column("groups", "embedding_updated_at")
    op.drop_column("groups", "embedding")
