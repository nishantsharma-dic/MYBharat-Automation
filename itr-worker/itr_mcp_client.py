"""
ITR MCP Client Worker
Connects to MCP-India-Stack server and processes ITR data (income tax calculations,
TDS, advance tax, capital gains, etc.)
"""

import asyncio
import json
import logging
import os
from datetime import datetime
from pathlib import Path

from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client

# Setup logging
LOG_DIR = Path(__file__).parent / "logs"
LOG_DIR.mkdir(exist_ok=True)

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.FileHandler(LOG_DIR / "itr_worker.log"),
        logging.StreamHandler(),
    ],
)
logger = logging.getLogger(__name__)

# Reports output directory
REPORTS_DIR = Path(__file__).parent / "reports"
REPORTS_DIR.mkdir(exist_ok=True)


class ITRMCPClient:
    """Client that connects to MCP-India-Stack and processes ITR-related tasks."""

    def __init__(self):
        self.session = None
        self.server_params = StdioServerParameters(
            command="python3",
            args=["-m", "mcp_india_stack"],
        )

    async def connect(self):
        """Establish connection to MCP-India-Stack server."""
        logger.info("Connecting to MCP-India-Stack server...")
        self._client_context = stdio_client(self.server_params)
        self._streams = await self._client_context.__aenter__()
        read_stream, write_stream = self._streams
        self.session = ClientSession(read_stream, write_stream)
        await self.session.__aenter__()
        await self.session.initialize()
        logger.info("Connected to MCP-India-Stack server successfully.")

    async def disconnect(self):
        """Close connection to MCP server."""
        if self.session:
            await self.session.__aexit__(None, None, None)
        if self._client_context:
            await self._client_context.__aexit__(None, None, None)
        logger.info("Disconnected from MCP-India-Stack server.")

    async def list_tools(self):
        """List all available tools from the MCP server."""
        result = await self.session.list_tools()
        return [tool.name for tool in result.tools]

    async def call_tool(self, tool_name: str, arguments: dict):
        """Call a specific tool on the MCP server."""
        logger.info(f"Calling tool: {tool_name} with args: {json.dumps(arguments)}")
        result = await self.session.call_tool(tool_name, arguments)
        # Extract text content from result
        if result.content:
            for content_block in result.content:
                if hasattr(content_block, "text"):
                    return json.loads(content_block.text)
        return None

    # ─── ITR Processing Methods ───────────────────────────────────────────

    async def calculate_income_tax(self, taxpayer: dict) -> dict:
        """
        Calculate income tax for a taxpayer under both old and new regime.

        Args:
            taxpayer: dict with keys like income, deductions_80c, hra, etc.
        """
        args = {
            "gross_income": taxpayer.get("gross_income", 0),
            "deductions_80c": taxpayer.get("deductions_80c", 0),
            "deductions_80d": taxpayer.get("deductions_80d", 0),
            "hra_received": taxpayer.get("hra_received", 0),
            "age": taxpayer.get("age", 30),
        }
        return await self.call_tool("calculate_income_tax", args)

    async def calculate_tds(self, section: str, amount: float, pan_available: bool = True) -> dict:
        """Calculate TDS for a given section and amount."""
        args = {
            "section": section,
            "amount": amount,
            "pan_available": pan_available,
        }
        return await self.call_tool("calculate_tds", args)

    async def calculate_advance_tax(self, taxpayer: dict) -> dict:
        """Calculate advance tax installments for the financial year."""
        args = {
            "total_income": taxpayer.get("gross_income", 0),
            "tds_already_deducted": taxpayer.get("tds_deducted", 0),
        }
        return await self.call_tool("calculate_advance_tax", args)

    async def calculate_capital_gains(self, asset_type: str, purchase_price: float,
                                       sale_price: float, holding_months: int) -> dict:
        """Calculate capital gains tax."""
        args = {
            "asset_type": asset_type,
            "purchase_price": purchase_price,
            "sale_price": sale_price,
            "holding_period_months": holding_months,
        }
        return await self.call_tool("calculate_capital_gains", args)

    async def calculate_hra_exemption(self, basic_salary: float, hra_received: float,
                                       rent_paid: float, is_metro: bool) -> dict:
        """Calculate HRA exemption."""
        args = {
            "basic_salary": basic_salary,
            "hra_received": hra_received,
            "rent_paid": rent_paid,
            "metro_city": is_metro,
        }
        return await self.call_tool("calculate_hra_exemption", args)

    async def get_regulatory_deadlines(self) -> dict:
        """Get upcoming tax and regulatory deadlines."""
        return await self.call_tool("get_regulatory_deadlines", {})

    async def validate_pan(self, pan: str) -> dict:
        """Validate a PAN number."""
        return await self.call_tool("validate_pan", {"pan": pan})

    # ─── Batch Processing ─────────────────────────────────────────────────

    async def process_taxpayer(self, taxpayer: dict) -> dict:
        """
        Run full ITR processing for a single taxpayer.
        Returns a consolidated report.
        """
        report = {
            "taxpayer_name": taxpayer.get("name", "Unknown"),
            "pan": taxpayer.get("pan", "N/A"),
            "processed_at": datetime.now().isoformat(),
            "results": {},
        }

        # Validate PAN
        if taxpayer.get("pan"):
            try:
                pan_result = await self.validate_pan(taxpayer["pan"])
                report["results"]["pan_validation"] = pan_result
            except Exception as e:
                logger.error(f"PAN validation failed: {e}")
                report["results"]["pan_validation"] = {"error": str(e)}

        # Income Tax Calculation
        try:
            tax_result = await self.calculate_income_tax(taxpayer)
            report["results"]["income_tax"] = tax_result
        except Exception as e:
            logger.error(f"Income tax calculation failed: {e}")
            report["results"]["income_tax"] = {"error": str(e)}

        # Advance Tax
        try:
            advance_tax = await self.calculate_advance_tax(taxpayer)
            report["results"]["advance_tax"] = advance_tax
        except Exception as e:
            logger.error(f"Advance tax calculation failed: {e}")
            report["results"]["advance_tax"] = {"error": str(e)}

        # HRA Exemption (if applicable)
        if taxpayer.get("hra_received") and taxpayer.get("rent_paid"):
            try:
                hra = await self.calculate_hra_exemption(
                    basic_salary=taxpayer.get("basic_salary", 0),
                    hra_received=taxpayer["hra_received"],
                    rent_paid=taxpayer["rent_paid"],
                    is_metro=taxpayer.get("is_metro", False),
                )
                report["results"]["hra_exemption"] = hra
            except Exception as e:
                logger.error(f"HRA calculation failed: {e}")
                report["results"]["hra_exemption"] = {"error": str(e)}

        # Capital Gains (if applicable)
        if taxpayer.get("capital_gains"):
            for i, cg in enumerate(taxpayer["capital_gains"]):
                try:
                    cg_result = await self.calculate_capital_gains(
                        asset_type=cg.get("asset_type", "equity"),
                        purchase_price=cg.get("purchase_price", 0),
                        sale_price=cg.get("sale_price", 0),
                        holding_months=cg.get("holding_months", 12),
                    )
                    report["results"][f"capital_gains_{i+1}"] = cg_result
                except Exception as e:
                    logger.error(f"Capital gains calculation failed: {e}")
                    report["results"][f"capital_gains_{i+1}"] = {"error": str(e)}

        return report

    async def process_all_taxpayers(self, taxpayers: list) -> list:
        """Process ITR for all taxpayers in the list."""
        reports = []
        for taxpayer in taxpayers:
            logger.info(f"Processing taxpayer: {taxpayer.get('name', 'Unknown')}")
            report = await self.process_taxpayer(taxpayer)
            reports.append(report)
            logger.info(f"Completed processing for: {taxpayer.get('name', 'Unknown')}")
        return reports

    def save_reports(self, reports: list):
        """Save processing reports to JSON files."""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        report_file = REPORTS_DIR / f"itr_report_{timestamp}.json"
        with open(report_file, "w") as f:
            json.dump(reports, f, indent=2, default=str)
        logger.info(f"Reports saved to: {report_file}")
        return report_file


async def run_itr_processing(taxpayers: list):
    """
    Main entry point: connect to MCP server, process taxpayers, save reports.
    """
    client = ITRMCPClient()
    try:
        await client.connect()

        # List available tools for verification
        tools = await client.list_tools()
        logger.info(f"Available tools ({len(tools)}): {tools[:10]}...")

        # Process all taxpayers
        reports = await client.process_all_taxpayers(taxpayers)

        # Save reports
        report_file = client.save_reports(reports)
        logger.info(f"ITR processing complete. Report: {report_file}")

        return reports
    except Exception as e:
        logger.error(f"ITR processing failed: {e}", exc_info=True)
        raise
    finally:
        await client.disconnect()


if __name__ == "__main__":
    # Quick test with sample data
    sample_taxpayers = [
        {
            "name": "Test User",
            "pan": "ABCDE1234F",
            "gross_income": 1200000,
            "deductions_80c": 150000,
            "deductions_80d": 25000,
            "age": 35,
            "basic_salary": 600000,
            "hra_received": 240000,
            "rent_paid": 300000,
            "is_metro": True,
            "tds_deducted": 80000,
        }
    ]
    asyncio.run(run_itr_processing(sample_taxpayers))
