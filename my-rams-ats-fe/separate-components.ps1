# PowerShell script to separate Angular components
# This script will separate inline templates and styles from all Angular components

Write-Host "Starting Angular component separation process..." -ForegroundColor Green

$components = @(
    "src/app/components/users/user-management/user-management.component.ts",
    "src/app/components/master-data/role-management/role-management.component.ts",
    "src/app/components/contact/contact-form/contact-form.component.ts",
    "src/app/components/auth/forgot-password/forgot-password.component.ts",
    "src/app/components/auth/register/register.component.ts",
    "src/app/components/contact/contact-messages/contact-messages.component.ts",
    "src/app/components/user/profile/profile.component.ts",
    "src/app/components/public/landing-page/landing-page.component.ts",
    "src/app/components/master-data/job-function-management/job-function-management.component.ts",
    "src/app/components/master-data/experience-level-management/experience-level-management.component.ts",
    "src/app/components/master-data/location-management/location-management.component.ts",
    "src/app/components/master-data/menu-management/menu-management.component.ts",
    "src/app/components/master-data/job-type-management/job-type-management.component.ts",
    "src/app/components/master-data/department-management/department-management.component.ts",
    "src/app/components/layout/admin-layout/admin-layout.component.ts",
    "src/app/components/layout/navbar/navbar.component.ts",
    "src/app/components/layout/sidebar/sidebar.component.ts",
    "src/app/components/layout/user-layout/user-layout.component.ts",
    "src/app/components/dashboard/user-dashboard/user-dashboard.component.ts",
    "src/app/components/dashboard/dashboard.component.ts",
    "src/app/components/jobs/public-job-view/public-job-view.component.ts",
    "src/app/components/jobs/public-job-list/public-job-list.component.ts",
    "src/app/components/jobs/job-view/job-view.component.ts",
    "src/app/components/jobs/job-list/job-list.component.ts",
    "src/app/components/jobs/job-form/job-form.component.ts",
    "src/app/components/layout/public-layout/public-layout.component.ts"
)

function Extract-Template {
    param(
        [string]$filePath
    )
    
    $content = Get-Content $filePath -Raw
    
    # Extract template between template: ` and closing `
    $templatePattern = "template:\s*\`(.*?)\`\s*,?\s*(?:styles:|}\s*$)"
    $templateMatch = [regex]::Match($content, $templatePattern, [System.Text.RegularExpressions.RegexOptions]::Singleline)
    
    if ($templateMatch.Success) {
        return $templateMatch.Groups[1].Value.Trim()
    }
    
    return $null
}

function Extract-Styles {
    param(
        [string]$filePath
    )
    
    $content = Get-Content $filePath -Raw
    
    # Extract styles between styles: [ and closing ]
    $stylesPattern = "styles:\s*\[\s*\`(.*?)\`\s*\]"
    $stylesMatch = [regex]::Match($content, $stylesPattern, [System.Text.RegularExpressions.RegexOptions]::Singleline)
    
    if ($stylesMatch.Success) {
        return $stylesMatch.Groups[1].Value.Trim()
    }
    
    return ":host { display: block; }"
}

function Update-ComponentTs {
    param(
        [string]$filePath
    )
    
    $content = Get-Content $filePath -Raw
    $fileName = [System.IO.Path]::GetFileNameWithoutExtension($filePath)
    
    # Replace template with templateUrl
    $content = $content -replace "template:\s*\`.*?\`\s*,?", "templateUrl: './$fileName.html'"
    
    # Replace styles with styleUrls
    $content = $content -replace "styles:\s*\[\s*\`.*?\`\s*\]", "styleUrls: ['./$fileName.css']"
    
    Set-Content $filePath $content -Encoding UTF8
}

foreach ($componentPath in $components) {
    if (Test-Path $componentPath) {
        Write-Host "Processing: $componentPath" -ForegroundColor Yellow
        
        $directory = Split-Path $componentPath
        $fileName = [System.IO.Path]::GetFileNameWithoutExtension($componentPath)
        
        # Extract template and styles
        $template = Extract-Template $componentPath
        $styles = Extract-Styles $componentPath
        
        if ($template) {
            # Create HTML file
            $htmlPath = Join-Path $directory "$fileName.html"
            Set-Content $htmlPath $template -Encoding UTF8
            Write-Host "  Created: $htmlPath" -ForegroundColor Green
        }
        
        # Create CSS file
        $cssPath = Join-Path $directory "$fileName.css"
        Set-Content $cssPath $styles -Encoding UTF8
        Write-Host "  Created: $cssPath" -ForegroundColor Green
        
        # Create basic spec.ts file if it doesn't exist
        $specPath = Join-Path $directory "$fileName.spec.ts"
        if (-not (Test-Path $specPath)) {
            $componentName = (Get-Culture).TextInfo.ToTitleCase($fileName -replace '-', ' ') -replace ' ', ''
            $specContent = @"
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { $componentName } from './$fileName';

describe('$componentName', () => {
  let component: $componentName;
  let fixture: ComponentFixture<$componentName>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [$componentName]
    }).compileComponents();

    fixture = TestBed.createComponent($componentName);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
"@
            Set-Content $specPath $specContent -Encoding UTF8
            Write-Host "  Created: $specPath" -ForegroundColor Green
        }
        
        # Update the component TypeScript file
        Update-ComponentTs $componentPath
        Write-Host "  Updated: $componentPath" -ForegroundColor Green
        
    } else {
        Write-Host "File not found: $componentPath" -ForegroundColor Red
    }
}

Write-Host "Component separation completed!" -ForegroundColor Green
Write-Host "Please run 'ng build' or 'ng serve' to verify all components work correctly." -ForegroundColor Cyan 